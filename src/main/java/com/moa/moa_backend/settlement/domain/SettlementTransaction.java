package com.moa.moa_backend.settlement.domain;

import com.moa.moa_backend.transaction.domain.Transaction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settlement_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String categoryName;

    @Column(length = 100)
    private String paymentMethodName;
}
