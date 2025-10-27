package com.moa.moa_backend.settlement.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSettlementRequestDto {

    private Long accountBookId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> participantUserIds;
    private List<Long> transactionIds;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
}
