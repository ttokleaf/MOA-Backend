package com.moa.moa_backend.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "예산 분석 정보")
public class BudgetAnalysisDto {

    @Schema(description = "설정된 예산 금액", example = "500000")
    private Long budgetAmount;

    @Schema(description = "이번달 지출 총 합", example = "350000")
    private Long currentMonthExpense;

    @Schema(description = "예산 대비 사용 금액", example = "150000")
    private Long remainingAmount;

    @Schema(description = "예산 초과 금액 (초과 시에만)", example = "50000")
    private Long overAmount;

    @Schema(description = "예산 사용률 (%)", example = "70.0")
    private Double usageRate;

    @Schema(description = "예산 상태", example = "UNDER_BUDGET", allowableValues = {"UNDER_BUDGET", "OVER_BUDGET"})
    private BudgetStatus status;

    @Schema(description = "지난달 지출 총 합", example = "400000")
    private Long lastMonthExpense;

    @Schema(description = "최근 3개월 평균 지출", example = "375000")
    private Long averageExpense;

    @Schema(description = "년도", example = "2024")
    private Integer year;

    @Schema(description = "월", example = "12")
    private Integer month;

    public enum BudgetStatus {
        UNDER_BUDGET, // 예산 내
        OVER_BUDGET    // 예산 초과
    }
}
