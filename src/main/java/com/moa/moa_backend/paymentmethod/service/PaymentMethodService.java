package com.moa.moa_backend.paymentmethod.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import org.springframework.context.annotation.Lazy;
import com.moa.moa_backend.paymentmethod.dto.CreatePaymentMethodRequestDto;
import com.moa.moa_backend.paymentmethod.dto.PaymentMethodResponseDto;
import com.moa.moa_backend.paymentmethod.dto.UpdatePaymentMethodRequestDto;
import com.moa.moa_backend.paymentmethod.repository.PaymentMethodRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserService userService;
    private final AccountBookService accountBookService;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository, 
                               UserService userService, 
                               @Lazy AccountBookService accountBookService) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userService = userService;
        this.accountBookService = accountBookService;
    }

    @Transactional
    public PaymentMethodResponseDto createPaymentMethod(HttpSession session, Long accountBookId, CreatePaymentMethodRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 가계부 조회 및 권한 확인
        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 중복 이름 확인
        if (paymentMethodRepository.existsByAccountBookAndNameAndIsActiveTrue(accountBook, request.getName())) {
            throw new RuntimeException("이미 존재하는 결제 수단 이름입니다.");
        }

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(request.getName())
                .accountBook(accountBook)
                .createdBy(currentUser)
                .isActive(true)
                .build();

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        
        log.info("결제 수단 생성 완료 - 가계부 ID: {}, 사용자 ID: {}, 결제 수단 ID: {}, 이름: {}", 
                accountBookId, currentUser.getId(), savedPaymentMethod.getId(), savedPaymentMethod.getName());

        return PaymentMethodResponseDto.from(savedPaymentMethod);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponseDto> getPaymentMethods(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 가계부 조회 및 권한 확인
        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByAccountBookAndIsActiveTrueOrderByCreatedAtDesc(accountBook);
        
        return paymentMethods.stream()
                .map(PaymentMethodResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentMethodResponseDto updatePaymentMethod(HttpSession session, Long accountBookId, Long id, UpdatePaymentMethodRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 가계부 조회 및 권한 확인
        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndAccountBook(id, accountBook)
                .orElseThrow(() -> new RuntimeException("결제 수단을 찾을 수 없습니다."));

        // 이름 변경 시 중복 확인
        if (!paymentMethod.getName().equals(request.getName()) && 
            paymentMethodRepository.existsByAccountBookAndNameAndIsActiveTrue(accountBook, request.getName())) {
            throw new RuntimeException("이미 존재하는 결제 수단 이름입니다.");
        }

        paymentMethod.setName(request.getName());

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        
        log.info("결제 수단 수정 완료 - 가계부 ID: {}, 사용자 ID: {}, 결제 수단 ID: {}, 이름: {}", 
                accountBookId, currentUser.getId(), savedPaymentMethod.getId(), savedPaymentMethod.getName());

        return PaymentMethodResponseDto.from(savedPaymentMethod);
    }

    @Transactional
    public void deletePaymentMethod(HttpSession session, Long accountBookId, Long id) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 가계부 조회 및 권한 확인
        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndAccountBook(id, accountBook)
                .orElseThrow(() -> new RuntimeException("결제 수단을 찾을 수 없습니다."));

        // 실제 삭제 대신 비활성화 처리
        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
        
        log.info("결제 수단 삭제 완료 - 가계부 ID: {}, 사용자 ID: {}, 결제 수단 ID: {}, 이름: {}", 
                accountBookId, currentUser.getId(), paymentMethod.getId(), paymentMethod.getName());
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponseDto getPaymentMethod(HttpSession session, Long accountBookId, Long id) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 가계부 조회 및 권한 확인
        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndAccountBook(id, accountBook)
                .orElseThrow(() -> new RuntimeException("결제 수단을 찾을 수 없습니다."));

        return PaymentMethodResponseDto.from(paymentMethod);
    }

    /**
     * 가계부에 기본 결제 수단들을 생성합니다.
     * 현금, 은행, 신용카드 기본값을 제공합니다.
     */
    @Transactional
    public void createDefaultPaymentMethods(AccountBook accountBook, User createdBy) {
        // 이미 기본 결제 수단이 있는지 확인
        long existingCount = paymentMethodRepository.countByAccountBookAndIsActiveTrue(accountBook);
        if (existingCount > 0) {
            log.info("가계부 {}는 이미 결제 수단이 존재합니다. 기본 결제 수단 생성을 건너뜁니다.", accountBook.getId());
            return;
        }

        // 기본 결제 수단 정의
        List<String> defaultMethods = Arrays.asList("현금", "은행", "신용카드");

        // 기본 결제 수단들 생성
        for (String methodName : defaultMethods) {
            PaymentMethod paymentMethod = PaymentMethod.builder()
                    .name(methodName)
                    .accountBook(accountBook)
                    .createdBy(createdBy)
                    .isActive(true)
                    .build();

            paymentMethodRepository.save(paymentMethod);
            log.info("기본 결제 수단 생성 완료 - 가계부 ID: {}, 결제 수단: {}", accountBook.getId(), methodName);
        }

        log.info("가계부 {}의 기본 결제 수단 생성 완료", accountBook.getId());
    }

}
