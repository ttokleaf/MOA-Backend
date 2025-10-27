package com.moa.moa_backend.settlement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementParticipantSummaryDto {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private Long amountToPay;
    private Boolean isPaid;
}
