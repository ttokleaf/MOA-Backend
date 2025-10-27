package com.moa.moa_backend.settlement.controller;

import com.moa.moa_backend.settlement.dto.*;
import com.moa.moa_backend.settlement.service.SettlementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 정산 미리보기 생성
     * POST /api/settlements/preview
     */
    @PostMapping("/preview")
    public ResponseEntity<SettlementPreviewDto> createSettlementPreview(
            @RequestBody CreateSettlementRequestDto requestDto,
            HttpSession session) {
        log.info("정산 미리보기 생성 요청: {}", requestDto);
        
        SettlementPreviewDto preview = settlementService.createSettlementPreview(requestDto, session);
        return ResponseEntity.ok(preview);
    }

    /**
     * 정산 생성
     * POST /api/settlements
     */
    @PostMapping
    public ResponseEntity<SettlementResponseDto> createSettlement(
            @RequestBody CreateSettlementRequestDto requestDto,
            HttpSession session) {
        log.info("정산 생성 요청: {}", requestDto);
        
        SettlementResponseDto settlement = settlementService.createSettlement(requestDto, session);
        return ResponseEntity.ok(settlement);
    }

    /**
     * 가계부별 정산 목록 조회
     * GET /api/settlements/account-book/{accountBookId}
     */
    @GetMapping("/account-book/{accountBookId}")
    public ResponseEntity<List<SettlementSummaryDto>> getSettlementsByAccountBook(
            @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        log.info("가계부별 정산 목록 조회 요청: {}", accountBookId);
        
        List<SettlementSummaryDto> settlements = settlementService.getSettlementsByAccountBook(accountBookId, session);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 공유 코드로 정산 상세 조회
     * GET /api/settlements/share/{shareCode}
     */
    @GetMapping("/share/{shareCode}")
    public ResponseEntity<SettlementResponseDto> getSettlementByShareCode(
            @PathVariable("shareCode") String shareCode) {
        log.info("공유 코드로 정산 조회 요청: {}", shareCode);
        
        SettlementResponseDto settlement = settlementService.getSettlementByShareCode(shareCode);
        return ResponseEntity.ok(settlement);
    }

    /**
     * 사용자별 정산 목록 조회
     * GET /api/settlements/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SettlementSummaryDto>> getSettlementsByUserId(
            @PathVariable("userId") Long userId) {
        log.info("사용자별 정산 목록 조회 요청: {}", userId);
        
        List<SettlementSummaryDto> settlements = settlementService.getSettlementsByUserId(userId);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 정산 참여자 결제 상태 업데이트
     * PATCH /api/settlements/{settlementId}/participants/{userId}/payment
     */
    @PatchMapping("/{settlementId}/participants/{userId}/payment")
    public ResponseEntity<Void> updateParticipantPaymentStatus(
            @PathVariable("settlementId") Long settlementId,
            @PathVariable("userId") Long userId,
            @RequestBody PaymentStatusUpdateDto paymentStatusDto,
            HttpSession session) {
        log.info("정산 참여자 결제 상태 업데이트 요청 - 정산 ID: {}, 사용자 ID: {}, 결제 상태: {}", 
                settlementId, userId, paymentStatusDto.getIsPaid());
        
        settlementService.updateParticipantPaymentStatus(settlementId, userId, paymentStatusDto.getIsPaid(), session);
        return ResponseEntity.ok().build();
    }

    /**
     * 정산 삭제
     * DELETE /api/settlements/{settlementId}
     */
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<Void> deleteSettlement(
            @PathVariable("settlementId") Long settlementId,
            HttpSession session) {
        log.info("정산 삭제 요청 - 정산 ID: {}", settlementId);
        
        settlementService.deleteSettlement(settlementId, session);
        return ResponseEntity.ok().build();
    }
}
