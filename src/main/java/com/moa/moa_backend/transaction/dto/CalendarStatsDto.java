package com.moa.moa_backend.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarStatsDto {
    
    // 총 수입
    private Long totalIncome;
    
    // 총 지출
    private Long totalExpense;
    
    // 총합 (수입 - 지출)
    private Long total;
    
    // 총 저축
    private Long totalSavings;
    
    // 예산에서 제외된 총 합계
    private Long totalExcludedFromBudget;
    
    // 조회 기간
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 월별 통계인 경우
    private Integer year;
    private Integer month;
}
