package com.moa.moa_backend.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "거래 내역 통계 응답")
public class TransactionStatsDto {

    @Schema(description = "총 수입", example = "500000")
    private Long totalIncome;

    @Schema(description = "총 지출", example = "300000")
    private Long totalExpense;

    @Schema(description = "총 저축", example = "100000")
    private Long totalSavings;

    @Schema(description = "순이익 (수입 - 지출)", example = "200000")
    private Long netIncome;

    @Schema(description = "통계 기간 시작일", example = "2024-12-01")
    private LocalDate startDate;

    @Schema(description = "통계 기간 종료일", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "거래 내역 총 개수", example = "50")
    private Long totalTransactionCount;
}

