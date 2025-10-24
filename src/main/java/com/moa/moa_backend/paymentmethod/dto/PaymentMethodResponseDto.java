package com.moa.moa_backend.paymentmethod.dto;

import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "결제 수단 응답")
public class PaymentMethodResponseDto {

    @Schema(description = "결제 수단 ID", example = "1")
    private Long id;

    @Schema(description = "결제 수단 이름", example = "신한카드")
    private String name;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성일시", example = "2024-12-19T10:30:00")
    private LocalDateTime createdAt;

    public static PaymentMethodResponseDto from(PaymentMethod paymentMethod) {
        return PaymentMethodResponseDto.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .isActive(paymentMethod.getIsActive())
                .createdAt(paymentMethod.getCreatedAt())
                .build();
    }
}
