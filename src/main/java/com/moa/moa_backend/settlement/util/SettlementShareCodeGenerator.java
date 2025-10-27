package com.moa.moa_backend.settlement.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SettlementShareCodeGenerator {

    /**
     * 정산 공유를 위한 고유 코드 생성
     * UUID를 사용하여 중복 가능성을 최소화
     */
    public String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 공유 코드 유효성 검증
     */
    public boolean isValidShareCode(String shareCode) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            return false;
        }
        
        // UUID 형식 검증 (하이픈 제거된 32자리)
        return shareCode.matches("^[a-f0-9]{32}$");
    }
}
