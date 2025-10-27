package com.moa.moa_backend.settlement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementSummaryDto {

    private Long id;
    private Long accountBookId;
    private String accountBookName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalAmount;
    private String shareCode;
    private String createdByNickname;
    private LocalDateTime createdAt;
    private List<SettlementParticipantSummaryDto> participants;
}
