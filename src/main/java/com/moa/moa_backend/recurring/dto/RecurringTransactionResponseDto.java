package com.moa.moa_backend.recurring.dto;

import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.dto.TransactionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "반복 거래 응답")
public class RecurringTransactionResponseDto {

    @Schema(description = "반복 거래 ID", example = "1")
    private Long id;

    @Schema(description = "반복 타입", example = "MONTHLY")
    private RecurringType recurringType;

    @Schema(description = "시작 날짜", example = "2024-12-19")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-12-19")
    private LocalDate endDate;

    @Schema(description = "마지막 생성 날짜", example = "2024-12-19")
    private LocalDate lastGeneratedDate;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성일", example = "2024-12-19T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-12-19T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "템플릿 거래 정보")
    private TransactionResponseDto templateTransaction;
}
