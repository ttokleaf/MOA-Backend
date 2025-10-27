package com.moa.moa_backend.settlement.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementPreviewDto {

    private Long accountBookId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<SettlementTransactionDto> transactions;
    private List<SettlementParticipantDto> participants;
    private Long totalAmount;
}
