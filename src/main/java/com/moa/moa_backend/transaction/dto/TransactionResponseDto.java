package com.moa.moa_backend.transaction.dto;

import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.transaction.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "거래 내역 응답")
public class TransactionResponseDto {

    @Schema(description = "거래 ID", example = "1")
    private Long id;


    @Schema(description = "거래 설명", example = "회사 근처 식당에서 점심")
    private String description;

    @Schema(description = "거래 금액", example = "15000")
    private Long amount;

    @Schema(description = "거래 타입", example = "EXPENSE")
    private TransactionType type;

    @Schema(description = "거래 날짜", example = "2024-12-19")
    private LocalDate transactionDate;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "식비")
    private String categoryName;

    @Schema(description = "결제 수단 ID", example = "1")
    private Long paymentMethodId;

    @Schema(description = "결제 수단 이름", example = "현금")
    private String paymentMethodName;

    @Schema(description = "메모", example = "회사 동료들과 함께")
    private String memo;

    @Schema(description = "예산에서 제외 여부", example = "false")
    private Boolean isExcludedFromBudget;

    @Schema(description = "반복 타입", example = "NONE")
    private RecurringType recurringType;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성일시", example = "2024-12-19T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "작성자 ID", example = "1")
    private Long createdById;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String createdByNickname;

    public static TransactionResponseDto from(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .paymentMethodId(transaction.getPaymentMethod().getId())
                .paymentMethodName(transaction.getPaymentMethod().getName())
                .memo(transaction.getMemo())
                .isExcludedFromBudget(transaction.getIsExcludedFromBudget())
                .recurringType(transaction.getRecurringType())
                .isActive(transaction.getIsActive())
                .createdAt(transaction.getCreatedAt())
                .createdById(transaction.getCreatedBy().getId())
                .createdByNickname(transaction.getCreatedBy().getNickname())
                .build();
    }
}

