package com.moa.moa_backend.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    
    // 전체 통계 (월별 또는 기간별)
    private CalendarStatsDto stats;
    
    // 일별 상세 정보
    private List<CalendarDayDto> days;
}
