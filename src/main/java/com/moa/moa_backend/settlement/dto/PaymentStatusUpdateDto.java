package com.moa.moa_backend.settlement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateDto {

    private Boolean isPaid;
}
