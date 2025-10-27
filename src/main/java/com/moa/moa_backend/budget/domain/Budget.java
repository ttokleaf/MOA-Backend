package com.moa.moa_backend.budget.domain;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id", nullable = false)
    private AccountBook accountBook;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Long amount; // 예산 금액 (정수 단위)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 예산이 설정된 날짜를 반환
     */
    public LocalDate getBudgetDate() {
        return LocalDate.of(year, month, 1);
    }

    /**
     * 예산이 설정된 년월을 문자열로 반환 (예: "2024-12")
     */
    public String getYearMonth() {
        return String.format("%d-%02d", year, month);
    }
}
