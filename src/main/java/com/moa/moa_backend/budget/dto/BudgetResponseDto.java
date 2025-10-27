package com.moa.moa_backend.budget.dto;

import com.moa.moa_backend.budget.domain.Budget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "예산 응답")
public class BudgetResponseDto {

    @Schema(description = "예산 ID", example = "1")
    private Long id;

    @Schema(description = "년도", example = "2024")
    private Integer year;

    @Schema(description = "월", example = "12")
    private Integer month;

    @Schema(description = "예산 금액", example = "500000")
    private Long amount;

    @Schema(description = "생성일시", example = "2024-12-19T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-12-19T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "작성자 ID", example = "1")
    private Long createdById;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String createdByNickname;

    public static BudgetResponseDto from(Budget budget) {
        return BudgetResponseDto.builder()
                .id(budget.getId())
                .year(budget.getYear())
                .month(budget.getMonth())
                .amount(budget.getAmount())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .createdById(budget.getCreatedBy().getId())
                .createdByNickname(budget.getCreatedBy().getNickname())
                .build();
    }
}
