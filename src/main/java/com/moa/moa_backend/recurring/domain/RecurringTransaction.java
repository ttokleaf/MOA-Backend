package com.moa.moa_backend.recurring.domain;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id", nullable = false)
    private AccountBook accountBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_transaction_id", nullable = false)
    private Transaction templateTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringType recurringType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private LocalDate lastGeneratedDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
