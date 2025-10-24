package com.moa.moa_backend.transaction.dto;

import com.moa.moa_backend.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransactionDto {
    
    // 거래 ID
    private Long id;
    
    // 거래 설명
    private String description;
    
    // 거래 금액
    private Long amount;
    
    // 거래 타입
    private TransactionType type;
    
    // 거래 날짜
    private LocalDate transactionDate;
    
    // 예산에서 제외 여부
    private Boolean isExcludedFromBudget;
    
    // 작성자 정보
    private Long createdById;
    private String createdByNickname;
    private String createdByProfileImageUrl;
    
    // 카테고리 정보
    private Long categoryId;
    private String categoryName;
    
    // 결제 수단 정보
    private Long paymentMethodId;
    private String paymentMethodName;
    
    // 메모
    private String memo;
}
