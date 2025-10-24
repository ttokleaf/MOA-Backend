package com.moa.moa_backend.paymentmethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "결제 수단 수정 요청")
public class UpdatePaymentMethodRequestDto {

    @NotBlank(message = "결제 수단 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "결제 수단 이름은 1자 이상 50자 이하여야 합니다.")
    @Schema(description = "결제 수단 이름", example = "신한카드", maxLength = 50)
    private String name;
}
