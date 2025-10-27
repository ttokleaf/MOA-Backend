package com.moa.moa_backend.settlement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementTransactionDto {

    private Long id;
    private Long transactionId;
    private String description;
    private Long amount;
    private String categoryName;
    private String paymentMethodName;
}
