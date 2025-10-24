package com.moa.moa_backend.paymentmethod.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import com.moa.moa_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    
    /**
     * 가계부별 활성화된 결제 수단 목록 조회
     */
    List<PaymentMethod> findByAccountBookAndIsActiveTrueOrderByCreatedAtDesc(AccountBook accountBook);
    
    /**
     * 가계부별 모든 결제 수단 목록 조회 (활성화/비활성화 포함)
     */
    List<PaymentMethod> findByAccountBookOrderByCreatedAtDesc(AccountBook accountBook);
    
    /**
     * 가계부별 결제 수단 이름 중복 확인
     */
    boolean existsByAccountBookAndNameAndIsActiveTrue(AccountBook accountBook, String name);
    
    /**
     * 가계부별 특정 결제 수단 조회
     */
    Optional<PaymentMethod> findByIdAndAccountBook(Long id, AccountBook accountBook);
    
    /**
     * 가계부별 결제 수단 개수 조회
     */
    long countByAccountBookAndIsActiveTrue(AccountBook accountBook);
    
    /**
     * 가계부와 생성자로 결제 수단 조회
     */
    Optional<PaymentMethod> findByIdAndAccountBookAndCreatedBy(Long id, AccountBook accountBook, User createdBy);
}
