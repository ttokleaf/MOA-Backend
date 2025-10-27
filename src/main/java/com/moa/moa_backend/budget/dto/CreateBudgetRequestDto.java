package com.moa.moa_backend.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "예산 생성 요청")
public class CreateBudgetRequestDto {

    @NotNull(message = "예산 금액은 필수입니다.")
    @Min(value = 0, message = "예산 금액은 0원 이상이어야 합니다.")
    @Schema(description = "예산 금액 (정수 단위)", example = "500000")
    private Long amount;
}
