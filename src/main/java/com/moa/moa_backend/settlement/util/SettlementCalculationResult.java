package com.moa.moa_backend.settlement.util;

import lombok.*;

import java.util.List;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementCalculationResult {

    private Long totalAmount;
    private int participantCount;
    private Long baseAmountPerPerson;
    private Long remainderAmount;
    private List<ParticipantAmount> participantAmounts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantAmount {
        private Long userId;
        private Long amountToPay;
        private boolean hasRemainder;
    }

    /**
     * 정산 금액을 참여자 수로 균등 분할하는 로직
     * 소수점은 랜덤한 한 명에게 추가로 부여
     */
    public static SettlementCalculationResult calculateSettlementAmounts(Long totalAmount, List<Long> participantUserIds) {
        if (participantUserIds.isEmpty()) {
            throw new IllegalArgumentException("참여자가 없습니다.");
        }

        int participantCount = participantUserIds.size();
        Long baseAmountPerPerson = totalAmount / participantCount;
        Long remainderAmount = totalAmount % participantCount;

        // 랜덤하게 나머지 금액을 받을 참여자 선택
        Random random = new Random();
        int remainderRecipientIndex = random.nextInt(participantCount);

        List<ParticipantAmount> participantAmounts = participantUserIds.stream()
                .map(userId -> {
                    boolean hasRemainder = participantUserIds.indexOf(userId) == remainderRecipientIndex;
                    Long amountToPay = baseAmountPerPerson + (hasRemainder ? remainderAmount : 0L);
                    
                    return ParticipantAmount.builder()
                            .userId(userId)
                            .amountToPay(amountToPay)
                            .hasRemainder(hasRemainder)
                            .build();
                })
                .toList();

        return SettlementCalculationResult.builder()
                .totalAmount(totalAmount)
                .participantCount(participantCount)
                .baseAmountPerPerson(baseAmountPerPerson)
                .remainderAmount(remainderAmount)
                .participantAmounts(participantAmounts)
                .build();
    }
}
