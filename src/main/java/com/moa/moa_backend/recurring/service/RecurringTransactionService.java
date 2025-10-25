package com.moa.moa_backend.recurring.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.repository.AccountBookRepository;
import com.moa.moa_backend.recurring.domain.RecurringTransaction;
import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.recurring.dto.RecurringTransactionResponseDto;
import com.moa.moa_backend.recurring.repository.RecurringTransactionRepository;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.transaction.dto.TransactionResponseDto;
import com.moa.moa_backend.transaction.repository.TransactionRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final AccountBookRepository accountBookRepository;
    private final UserService userService;

    /**
     * 반복 거래 생성 (엔티티 반환)
     */
    @Transactional
    public RecurringTransaction createRecurringTransaction(
            HttpSession session, 
            Long accountBookId, 
            Long templateTransactionId,
            RecurringType recurringType,
            LocalDate endDate) {
        
        User user = userService.getCurrentUser(session);
        if (user == null) {
            throw new IllegalArgumentException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }
        
        AccountBook accountBook = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        Transaction templateTransaction = transactionRepository.findById(templateTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("템플릿 거래를 찾을 수 없습니다."));

        // 권한 확인
        if (!accountBook.hasUser(user)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }

        RecurringTransaction recurringTransaction = RecurringTransaction.builder()
                .accountBook(accountBook)
                .templateTransaction(templateTransaction)
                .recurringType(recurringType)
                .startDate(templateTransaction.getTransactionDate()) // 거래 날짜를 시작 날짜로 사용
                .endDate(endDate)
                .createdBy(user)
                .isActive(true)
                .build();

        RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
        
        // 모든 반복 거래 즉시 생성 (미래까지 포함)
        generateAllTransactionsForRecurring(saved);

        return saved;
    }

    /**
     * 반복 거래 목록 조회
     */
    public List<RecurringTransactionResponseDto> getRecurringTransactions(HttpSession session, Long accountBookId) {
        User user = userService.getCurrentUser(session);
        if (user == null) {
            throw new IllegalArgumentException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }
        
        AccountBook accountBook = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));

        // 권한 확인
        if (!accountBook.hasUser(user)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }

        List<RecurringTransaction> recurringTransactions = 
                recurringTransactionRepository.findByAccountBookIdAndActive(accountBookId);

        return recurringTransactions.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 반복 거래 비활성화
     */
    @Transactional
    public void deactivateRecurringTransaction(HttpSession session, Long accountBookId, Long recurringTransactionId) {
        User user = userService.getCurrentUser(session);
        if (user == null) {
            throw new IllegalArgumentException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }
        
        AccountBook accountBook = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));

        // 권한 확인
        if (!accountBook.hasUser(user)) {
            throw new IllegalArgumentException("해당 가계부에 접근 권한이 없습니다.");
        }

        RecurringTransaction recurringTransaction = recurringTransactionRepository.findById(recurringTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));

        // 미래 복제 거래들도 함께 삭제
        deleteFutureRecurringTransactions(recurringTransaction);

        recurringTransaction.setIsActive(false);
        recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * 반복 거래 설정 업데이트
     */
    @Transactional
    public void updateRecurringTransactionSettings(Long recurringTransactionId, RecurringType recurringType, LocalDate endDate) {
        RecurringTransaction recurringTransaction = recurringTransactionRepository.findById(recurringTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("반복 거래를 찾을 수 없습니다."));

        recurringTransaction.setRecurringType(recurringType);
        if (endDate != null) {
            recurringTransaction.setEndDate(endDate);
        }
        
        recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * 반복 거래에 대한 모든 거래 생성 (즉시 생성)
     */
    @Transactional
    public void generateAllTransactionsForRecurring(RecurringTransaction recurringTransaction) {
        LocalDate startDate = recurringTransaction.getStartDate();
        LocalDate endDate = recurringTransaction.getEndDate();

        // 시작 날짜부터 종료 날짜까지 모든 거래 생성
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            createTransactionFromTemplate(recurringTransaction, currentDate);
            currentDate = calculateNextDate(currentDate, recurringTransaction.getRecurringType());
            
            if (currentDate == null) {
                break; // 더 이상 생성할 날짜가 없음
            }
        }

        // 마지막 생성 날짜 업데이트
        recurringTransaction.setLastGeneratedDate(endDate);
        recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * 다음 반복 날짜 계산
     */
    private LocalDate calculateNextDate(LocalDate currentDate, com.moa.moa_backend.transaction.domain.RecurringType recurringType) {
        return switch (recurringType) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
            case NONE -> null;
        };
    }

    /**
     * 템플릿 거래를 기반으로 실제 거래 생성
     */
    private void createTransactionFromTemplate(RecurringTransaction recurringTransaction, LocalDate transactionDate) {
        Transaction template = recurringTransaction.getTemplateTransaction();
        
        Transaction newTransaction = Transaction.builder()
                .description(template.getDescription())
                .amount(template.getAmount())
                .type(template.getType())
                .transactionDate(transactionDate)
                .accountBook(recurringTransaction.getAccountBook())
                .category(template.getCategory())
                .paymentMethod(template.getPaymentMethod())
                .createdBy(recurringTransaction.getCreatedBy())
                .memo(template.getMemo())
                .isExcludedFromBudget(template.getIsExcludedFromBudget())
                .recurringType(com.moa.moa_backend.transaction.domain.RecurringType.NONE) // 생성된 거래는 반복 타입이 NONE
                .recurringTransaction(recurringTransaction) // 원본 반복 거래와 연결
                .isOriginalRecurring(false) // 생성된 거래는 원본이 아님
                .isActive(true)
                .build();

        transactionRepository.save(newTransaction);
    }


    /**
     * 엔티티를 응답 DTO로 변환
     */
    private RecurringTransactionResponseDto convertToResponseDto(RecurringTransaction recurringTransaction) {
        TransactionResponseDto templateTransactionDto = TransactionResponseDto.from(recurringTransaction.getTemplateTransaction());
        
        return RecurringTransactionResponseDto.builder()
                .id(recurringTransaction.getId())
                .recurringType(recurringTransaction.getRecurringType())
                .startDate(recurringTransaction.getStartDate())
                .endDate(recurringTransaction.getEndDate())
                .lastGeneratedDate(recurringTransaction.getLastGeneratedDate())
                .isActive(recurringTransaction.getIsActive())
                .createdAt(recurringTransaction.getCreatedAt())
                .updatedAt(recurringTransaction.getUpdatedAt())
                .templateTransaction(templateTransactionDto)
                .build();
    }

    /**
     * 미래 복제 거래들을 삭제 처리
     */
    private void deleteFutureRecurringTransactions(RecurringTransaction recurringTransaction) {
        LocalDate currentDate = LocalDate.now();
        List<Transaction> futureTransactions = transactionRepository.findByRecurringTransactionAndFutureDate(
                recurringTransaction, currentDate);
        
        for (Transaction futureTransaction : futureTransactions) {
            futureTransaction.setIsActive(false);
            transactionRepository.save(futureTransaction);
        }
        
        log.info("미래 복제 거래 {}개 삭제 완료 - 반복 거래 ID: {}", 
                futureTransactions.size(), recurringTransaction.getId());
    }

}
