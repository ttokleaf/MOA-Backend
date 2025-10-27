package com.moa.moa_backend.settlement.domain;

import com.moa.moa_backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settlement_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long amountToPay;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPaid = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
}
