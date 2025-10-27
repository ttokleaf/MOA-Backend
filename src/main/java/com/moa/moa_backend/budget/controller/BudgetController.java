package com.moa.moa_backend.budget.controller;

import com.moa.moa_backend.budget.dto.*;
import com.moa.moa_backend.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/accountbooks/{accountBookId}/budgets")
@RequiredArgsConstructor
@Tag(name = "예산 관리", description = "예산 설정 및 분석 API")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 예산 생성 (현재 년월로 자동 설정)
     */
    @PostMapping
    @Operation(summary = "예산 생성", description = "현재 년월로 새로운 예산을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이번 달에 이미 예산이 설정됨")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetResponseDto> createBudget(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Valid @RequestBody CreateBudgetRequestDto request,
            HttpSession session) {
        
        BudgetResponseDto response = budgetService.createBudget(session, accountBookId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 년월 예산 수정
     */
    @PutMapping
    @Operation(summary = "현재 년월 예산 수정", description = "현재 년월의 예산을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetResponseDto> updateCurrentBudget(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Valid @RequestBody UpdateBudgetRequestDto request,
            HttpSession session) {
        
        BudgetResponseDto response = budgetService.updateCurrentBudget(session, accountBookId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 년월 예산 조회
     */
    @GetMapping("/current")
    @Operation(summary = "현재 년월 예산 조회", description = "현재 년월의 예산을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetResponseDto> getCurrentBudget(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        
        BudgetResponseDto response = budgetService.getCurrentBudget(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    /**
     * 예산 조회 (특정 년월)
     */
    @GetMapping("/{year}/{month}")
    @Operation(summary = "예산 조회", description = "특정 년월의 예산을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetResponseDto> getBudget(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "년도", required = true) @PathVariable("year") Integer year,
            @Parameter(description = "월", required = true) @PathVariable("month") Integer month,
            HttpSession session) {
        
        BudgetResponseDto response = budgetService.getBudget(session, accountBookId, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * 예산 목록 조회
     */
    @GetMapping
    @Operation(summary = "예산 목록 조회", description = "가계부의 모든 예산을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<List<BudgetResponseDto>> getBudgets(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        
        List<BudgetResponseDto> response = budgetService.getBudgets(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 년월 예산 분석
     */
    @GetMapping("/current/analysis")
    @Operation(summary = "현재 년월 예산 분석", description = "현재 년월의 예산 분석 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 분석 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetAnalysisDto> getCurrentBudgetAnalysis(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        
        BudgetAnalysisDto response = budgetService.getCurrentBudgetAnalysis(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    /**
     * 예산 분석 (특정 년월)
     */
    @GetMapping("/{year}/{month}/analysis")
    @Operation(summary = "예산 분석", description = "특정 년월의 예산 분석 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 분석 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<BudgetAnalysisDto> getBudgetAnalysis(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "년도", required = true) @PathVariable("year") Integer year,
            @Parameter(description = "월", required = true) @PathVariable("month") Integer month,
            HttpSession session) {
        
        BudgetAnalysisDto response = budgetService.getBudgetAnalysis(session, accountBookId, year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * 예산 삭제
     */
    @DeleteMapping("/{budgetId}")
    @Operation(summary = "예산 삭제", description = "기존 예산을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예산 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 예산을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "예산 ID", required = true) @PathVariable("budgetId") Long budgetId,
            HttpSession session) {
        
        budgetService.deleteBudget(session, accountBookId, budgetId);
        return ResponseEntity.ok().build();
    }
}
