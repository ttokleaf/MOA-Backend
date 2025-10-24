package com.moa.moa_backend.transaction.dto;

import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "거래 내역 생성 요청")
public class CreateTransactionRequestDto {


    @Size(max = 500, message = "거래 설명은 500자 이하여야 합니다.")
    @Schema(description = "거래 설명", example = "회사 근처 식당에서 점심", maxLength = 500)
    private String description;

    @NotNull(message = "거래 금액은 필수입니다.")
    @Min(value = 1, message = "거래 금액은 1원 이상이어야 합니다.")
    @Schema(description = "거래 금액", example = "15000")
    private Long amount;

    @NotNull(message = "거래 타입은 필수입니다.")
    @Schema(description = "거래 타입", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE", "SAVINGS"})
    private TransactionType type;

    @NotNull(message = "거래 날짜는 필수입니다.")
    @Schema(description = "거래 날짜", example = "2024-12-19")
    private LocalDate transactionDate;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @NotNull(message = "결제 수단 ID는 필수입니다.")
    @Schema(description = "결제 수단 ID", example = "1")
    private Long paymentMethodId;

    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다.")
    @Schema(description = "메모", example = "회사 동료들과 함께", maxLength = 1000)
    private String memo;

    @Schema(description = "예산에서 제외 여부", example = "false")
    private Boolean isExcludedFromBudget = false;

    @Schema(description = "반복 타입", example = "NONE", allowableValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"})
    private RecurringType recurringType = RecurringType.NONE;
}

