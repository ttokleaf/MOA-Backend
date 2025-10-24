package com.moa.moa_backend.transaction.domain;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.category.domain.Category;
import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import com.moa.moa_backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id", nullable = false)
    private AccountBook accountBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(length = 1000)
    private String memo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isExcludedFromBudget = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private RecurringType recurringType = RecurringType.NONE;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

