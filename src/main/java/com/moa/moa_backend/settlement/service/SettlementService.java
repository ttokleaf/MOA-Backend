package com.moa.moa_backend.settlement.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.repository.AccountBookRepository;
import com.moa.moa_backend.settlement.domain.Settlement;
import com.moa.moa_backend.settlement.domain.SettlementParticipant;
import com.moa.moa_backend.settlement.domain.SettlementTransaction;
import com.moa.moa_backend.settlement.dto.*;
import com.moa.moa_backend.settlement.repository.SettlementParticipantRepository;
import com.moa.moa_backend.settlement.repository.SettlementRepository;
import com.moa.moa_backend.settlement.util.SettlementCalculationResult;
import com.moa.moa_backend.settlement.util.SettlementShareCodeGenerator;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.transaction.repository.TransactionRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.repository.UserRepository;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementParticipantRepository settlementParticipantRepository;
    private final TransactionRepository transactionRepository;
    private final AccountBookRepository accountBookRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SettlementShareCodeGenerator shareCodeGenerator;

    /**
     * 정산 미리보기 생성
     */
    public SettlementPreviewDto createSettlementPreview(CreateSettlementRequestDto requestDto, HttpSession session) {
        // 가계부 조회
        AccountBook accountBook = accountBookRepository.findById(requestDto.getAccountBookId())
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));

        // 거래 내역 조회
        List<Transaction> transactions = transactionRepository.findByIdIn(requestDto.getTransactionIds());
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("선택된 거래 내역이 없습니다.");
        }

        // 참여자 조회
        List<User> participants = userRepository.findByIdIn(requestDto.getParticipantUserIds());
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("참여자가 없습니다.");
        }

        // 거래 내역 DTO 변환
        List<SettlementTransactionDto> transactionDtos = transactions.stream()
                .map(this::convertToSettlementTransactionDto)
                .collect(Collectors.toList());

        // 총 금액 계산
        Long totalAmount = transactions.stream()
                .mapToLong(Transaction::getAmount)
                .sum();

        // 금액 분할 계산
        SettlementCalculationResult calculationResult = SettlementCalculationResult
                .calculateSettlementAmounts(totalAmount, requestDto.getParticipantUserIds());

        // 참여자 DTO 변환
        List<SettlementParticipantDto> participantDtos = calculationResult.getParticipantAmounts().stream()
                .map(participantAmount -> {
                    User user = participants.stream()
                            .filter(p -> p.getId().equals(participantAmount.getUserId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));
                    
                    return SettlementParticipantDto.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .amountToPay(participantAmount.getAmountToPay())
                            .isPaid(false)
                            .build();
                })
                .collect(Collectors.toList());

        return SettlementPreviewDto.builder()
                .accountBookId(accountBook.getId())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .transactions(transactionDtos)
                .participants(participantDtos)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 정산 생성
     */
    @Transactional
    public SettlementResponseDto createSettlement(CreateSettlementRequestDto requestDto, HttpSession session) {
        // 현재 사용자 조회
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        // 가계부 조회
        AccountBook accountBook = accountBookRepository.findById(requestDto.getAccountBookId())
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));

        // 거래 내역 조회
        List<Transaction> transactions = transactionRepository.findByIdIn(requestDto.getTransactionIds());
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("선택된 거래 내역이 없습니다.");
        }

        // 참여자 조회
        List<User> participants = userRepository.findByIdIn(requestDto.getParticipantUserIds());
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("참여자가 없습니다.");
        }

        // 총 금액 계산
        Long totalAmount = transactions.stream()
                .mapToLong(Transaction::getAmount)
                .sum();

        // 공유 코드 생성
        String shareCode = shareCodeGenerator.generateShareCode();

        // 정산 엔티티 생성
        Settlement settlement = Settlement.builder()
                .accountBook(accountBook)
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .totalAmount(totalAmount)
                .bankName(requestDto.getBankName())
                .accountNumber(requestDto.getAccountNumber())
                .accountHolder(requestDto.getAccountHolder())
                .shareCode(shareCode)
                .createdBy(currentUser)
                .isActive(true)
                .build();

        settlement = settlementRepository.save(settlement);

        // 정산 거래 내역 추가
        for (Transaction transaction : transactions) {
            SettlementTransaction settlementTransaction = SettlementTransaction.builder()
                    .settlement(settlement)
                    .transaction(transaction)
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .categoryName(transaction.getCategory().getName())
                    .paymentMethodName(transaction.getPaymentMethod().getName())
                    .build();
            
            settlement.addTransaction(settlementTransaction);
        }

        // 금액 분할 계산
        SettlementCalculationResult calculationResult = SettlementCalculationResult
                .calculateSettlementAmounts(totalAmount, requestDto.getParticipantUserIds());

        // 정산 참여자 추가
        for (SettlementCalculationResult.ParticipantAmount participantAmount : calculationResult.getParticipantAmounts()) {
            User user = participants.stream()
                    .filter(p -> p.getId().equals(participantAmount.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));

            SettlementParticipant settlementParticipant = SettlementParticipant.builder()
                    .settlement(settlement)
                    .user(user)
                    .amountToPay(participantAmount.getAmountToPay())
                    .isPaid(false)
                    .isActive(true)
                    .build();

            settlement.addParticipant(settlementParticipant);
        }

        settlementRepository.save(settlement);

        return convertToSettlementResponseDto(settlement);
    }

    /**
     * 가계부별 정산 목록 조회
     */
    public List<SettlementSummaryDto> getSettlementsByAccountBook(Long accountBookId, HttpSession session) {
        List<Settlement> settlements = settlementRepository
                .findByAccountBookIdAndIsActiveTrueOrderByCreatedAtDesc(accountBookId);

        return settlements.stream()
                .map(this::convertToSettlementSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 공유 코드로 정산 상세 조회
     */
    public SettlementResponseDto getSettlementByShareCode(String shareCode) {
        Settlement settlement = settlementRepository.findByShareCodeAndIsActiveTrue(shareCode)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        return convertToSettlementResponseDto(settlement);
    }

    /**
     * 사용자별 정산 목록 조회
     */
    public List<SettlementSummaryDto> getSettlementsByUserId(Long userId) {
        List<Settlement> settlements = settlementRepository.findSettlementsByParticipantUserId(userId);

        return settlements.stream()
                .map(this::convertToSettlementSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 정산 참여자 결제 상태 업데이트
     */
    @Transactional
    public void updateParticipantPaymentStatus(Long settlementId, Long userId, Boolean isPaid, HttpSession session) {
        SettlementParticipant participant = settlementParticipantRepository
                .findBySettlementIdAndUserIdAndIsActiveTrue(settlementId, userId)
                .orElseThrow(() -> new IllegalArgumentException("정산 참여자를 찾을 수 없습니다."));

        participant.setIsPaid(isPaid);
        settlementParticipantRepository.save(participant);
    }

    /**
     * 정산 삭제
     */
    @Transactional
    public void deleteSettlement(Long settlementId, HttpSession session) {
        // 현재 사용자 조회
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다."));

        // 생성자만 삭제 가능
        if (!settlement.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("정산을 삭제할 권한이 없습니다.");
        }

        settlement.setIsActive(false);
        settlementRepository.save(settlement);
    }

    // DTO 변환 메서드들
    private SettlementTransactionDto convertToSettlementTransactionDto(Transaction transaction) {
        return SettlementTransactionDto.builder()
                .transactionId(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .categoryName(transaction.getCategory().getName())
                .paymentMethodName(transaction.getPaymentMethod().getName())
                .build();
    }

    private SettlementResponseDto convertToSettlementResponseDto(Settlement settlement) {
        List<SettlementTransactionDto> transactionDtos = settlement.getSettlementTransactions().stream()
                .map(st -> SettlementTransactionDto.builder()
                        .id(st.getId())
                        .transactionId(st.getTransaction().getId())
                        .description(st.getDescription())
                        .amount(st.getAmount())
                        .categoryName(st.getCategoryName())
                        .paymentMethodName(st.getPaymentMethodName())
                        .build())
                .collect(Collectors.toList());

        List<SettlementParticipantDto> participantDtos = settlement.getParticipants().stream()
                .map(sp -> SettlementParticipantDto.builder()
                        .id(sp.getId())
                        .userId(sp.getUser().getId())
                        .nickname(sp.getUser().getNickname())
                        .profileImageUrl(sp.getUser().getProfileImageUrl())
                        .amountToPay(sp.getAmountToPay())
                        .isPaid(sp.getIsPaid())
                        .build())
                .collect(Collectors.toList());

        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .accountBookId(settlement.getAccountBook().getId())
                .accountBookName(settlement.getAccountBook().getName())
                .startDate(settlement.getStartDate())
                .endDate(settlement.getEndDate())
                .totalAmount(settlement.getTotalAmount())
                .bankName(settlement.getBankName())
                .accountNumber(settlement.getAccountNumber())
                .accountHolder(settlement.getAccountHolder())
                .shareCode(settlement.getShareCode())
                .createdByNickname(settlement.getCreatedBy().getNickname())
                .createdAt(settlement.getCreatedAt())
                .transactions(transactionDtos)
                .participants(participantDtos)
                .build();
    }

    private SettlementSummaryDto convertToSettlementSummaryDto(Settlement settlement) {
        List<SettlementParticipantSummaryDto> participantDtos = settlement.getParticipants().stream()
                .map(sp -> SettlementParticipantSummaryDto.builder()
                        .userId(sp.getUser().getId())
                        .nickname(sp.getUser().getNickname())
                        .profileImageUrl(sp.getUser().getProfileImageUrl())
                        .amountToPay(sp.getAmountToPay())
                        .isPaid(sp.getIsPaid())
                        .build())
                .collect(Collectors.toList());

        return SettlementSummaryDto.builder()
                .id(settlement.getId())
                .accountBookId(settlement.getAccountBook().getId())
                .accountBookName(settlement.getAccountBook().getName())
                .startDate(settlement.getStartDate())
                .endDate(settlement.getEndDate())
                .totalAmount(settlement.getTotalAmount())
                .shareCode(settlement.getShareCode())
                .createdByNickname(settlement.getCreatedBy().getNickname())
                .createdAt(settlement.getCreatedAt())
                .participants(participantDtos)
                .build();
    }
}
