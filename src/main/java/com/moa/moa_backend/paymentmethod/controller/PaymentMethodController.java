package com.moa.moa_backend.paymentmethod.controller;

import com.moa.moa_backend.paymentmethod.dto.CreatePaymentMethodRequestDto;
import com.moa.moa_backend.paymentmethod.dto.PaymentMethodResponseDto;
import com.moa.moa_backend.paymentmethod.dto.UpdatePaymentMethodRequestDto;
import com.moa.moa_backend.paymentmethod.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountbooks/{accountBookId}/payment-methods")
@RequiredArgsConstructor
@Tag(name = "PaymentMethod", description = "결제 수단 관리 API")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @Operation(summary = "결제 수단 생성", description = "새로운 결제 수단을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 수단 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 결제 수단 이름")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping
    public ResponseEntity<PaymentMethodResponseDto> createPaymentMethod(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Valid @RequestBody CreatePaymentMethodRequestDto request,
            HttpSession session) {
        PaymentMethodResponseDto response = paymentMethodService.createPaymentMethod(session, accountBookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 수단 목록 조회", description = "가계부의 활성화된 결제 수단 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping
    public ResponseEntity<List<PaymentMethodResponseDto>> getPaymentMethods(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        List<PaymentMethodResponseDto> response = paymentMethodService.getPaymentMethods(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 수단 상세 조회", description = "특정 결제 수단의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 결제 수단을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> getPaymentMethod(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "결제 수단 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        PaymentMethodResponseDto response = paymentMethodService.getPaymentMethod(session, accountBookId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 수단 수정", description = "기존 결제 수단의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 결제 수단을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 결제 수단 이름")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodResponseDto> updatePaymentMethod(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "결제 수단 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePaymentMethodRequestDto request,
            HttpSession session) {
        PaymentMethodResponseDto response = paymentMethodService.updatePaymentMethod(session, accountBookId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 수단 삭제", description = "기존 결제 수단을 삭제합니다. (비활성화 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 결제 수단을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "결제 수단 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        paymentMethodService.deletePaymentMethod(session, accountBookId, id);
        return ResponseEntity.ok().build();
    }
}
