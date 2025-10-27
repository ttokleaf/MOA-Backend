package com.moa.moa_backend.settlement.domain;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id", nullable = false)
    private AccountBook accountBook;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(length = 100)
    private String bankName;

    @Column(length = 50)
    private String accountNumber;

    @Column(length = 100)
    private String accountHolder;

    @Column(length = 36, unique = true)
    private String shareCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 정산에 포함된 거래 내역들
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SettlementTransaction> settlementTransactions = new ArrayList<>();

    // 정산 참여자들
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SettlementParticipant> participants = new ArrayList<>();

    // 정산에 거래 내역 추가
    public void addTransaction(SettlementTransaction settlementTransaction) {
        this.settlementTransactions.add(settlementTransaction);
        settlementTransaction.setSettlement(this);
    }

    // 정산 참여자 추가
    public void addParticipant(SettlementParticipant participant) {
        this.participants.add(participant);
        participant.setSettlement(this);
    }

    // 총 금액 계산
    public void calculateTotalAmount() {
        this.totalAmount = this.settlementTransactions.stream()
                .mapToLong(SettlementTransaction::getAmount)
                .sum();
    }
}
