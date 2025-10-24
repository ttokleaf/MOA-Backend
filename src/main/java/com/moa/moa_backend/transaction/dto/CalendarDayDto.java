package com.moa.moa_backend.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDayDto {
    
    // 날짜
    private LocalDate date;
    
    // 해당 일의 총 수입
    private Long totalIncome;
    
    // 해당 일의 총 지출
    private Long totalExpense;
    
    // 해당 일의 예산에서 제외된 총 합계
    private Long totalExcludedFromBudget;
    
    // 해당 일의 거래 내역 목록
    private List<DailyTransactionDto> transactions;
}
