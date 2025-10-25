package com.moa.moa_backend.transaction.controller;

import com.moa.moa_backend.recurring.dto.RecurringTransactionResponseDto;
import com.moa.moa_backend.recurring.service.RecurringTransactionService;
import com.moa.moa_backend.transaction.domain.TransactionType;
import com.moa.moa_backend.transaction.dto.*;
import com.moa.moa_backend.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accountbooks/{accountBookId}/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "거래 내역 관리 API")
public class TransactionController {

    private final TransactionService transactionService;
    private final RecurringTransactionService recurringTransactionService;

    @Operation(summary = "거래 내역 생성", description = "새로운 거래 내역을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 내역 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부, 카테고리 또는 결제 수단을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Valid @RequestBody CreateTransactionRequestDto request,
            HttpSession session) {
        TransactionResponseDto response = transactionService.createTransaction(session, accountBookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 목록 조회", description = "가계부의 거래 내역 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        List<TransactionResponseDto> response = transactionService.getTransactions(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 페이징 조회", description = "가계부의 거래 내역을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/paged")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsWithPaging(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            Pageable pageable,
            HttpSession session) {
        Page<TransactionResponseDto> response = transactionService.getTransactionsWithPaging(session, accountBookId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 상세 조회", description = "특정 거래 내역의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 거래 내역을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransaction(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "거래 내역 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        TransactionResponseDto response = transactionService.getTransaction(session, accountBookId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 수정", description = "기존 거래 내역을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 거래 내역을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "거래 내역 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionRequestDto request,
            HttpSession session) {
        TransactionResponseDto response = transactionService.updateTransaction(session, accountBookId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 삭제", description = "기존 거래 내역을 삭제합니다. (비활성화 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 거래 내역을 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "거래 내역 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        transactionService.deleteTransaction(session, accountBookId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기간별 거래 내역 조회", description = "특정 기간의 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByDateRange(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "시작 날짜", required = true) @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜", required = true) @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        List<TransactionResponseDto> response = transactionService.getTransactionsByDateRange(session, accountBookId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월별 거래 내역 조회", description = "특정 월의 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/month")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByMonth(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "년도", required = true) @RequestParam("year") int year,
            @Parameter(description = "월", required = true) @RequestParam("month") int month,
            HttpSession session) {
        List<TransactionResponseDto> response = transactionService.getTransactionsByMonth(session, accountBookId, year, month);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 타입별 내역 조회", description = "특정 거래 타입의 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByType(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "거래 타입", required = true) @PathVariable("type") TransactionType type,
            HttpSession session) {
        List<TransactionResponseDto> response = transactionService.getTransactionsByType(session, accountBookId, type);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 통계 조회", description = "가계부의 거래 내역 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/stats")
    public ResponseEntity<TransactionStatsDto> getTransactionStats(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "시작 날짜", required = true) @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜", required = true) @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        TransactionStatsDto response = transactionService.getTransactionStats(session, accountBookId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월별 달력 데이터 조회", description = "특정 월의 달력 데이터를 조회합니다. 총 수입, 총 지출, total, 예산 제외 합계와 일별 상세 정보를 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/calendar/month")
    public ResponseEntity<CalendarResponseDto> getCalendarByMonth(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "년도", required = true) @RequestParam("year") int year,
            @Parameter(description = "월", required = true) @RequestParam("month") int month,
            HttpSession session) {
        CalendarResponseDto response = transactionService.getCalendarByMonth(session, accountBookId, year, month);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기간별 달력 데이터 조회", description = "특정 기간의 달력 데이터를 조회합니다. 총 수입, 총 지출, total, 예산 제외 합계와 일별 상세 정보를 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/calendar/date-range")
    public ResponseEntity<CalendarResponseDto> getCalendarByDateRange(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "시작 날짜", required = true) @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜", required = true) @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        CalendarResponseDto response = transactionService.getCalendarByDateRange(session, accountBookId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일별 거래 내역 조회", description = "특정 날짜의 거래 내역을 조회합니다. 작성자 프로필, 닉네임, 거래 설명, 가격, 타입, 예산제외여부, 카테고리, 결제수단 정보를 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/calendar/daily")
    public ResponseEntity<List<DailyTransactionDto>> getDailyTransactions(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "조회 날짜", required = true) @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session) {
        List<DailyTransactionDto> response = transactionService.getDailyTransactions(session, accountBookId, date);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "반복 거래 목록 조회", description = "가계부의 반복 거래 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/recurring")
    public ResponseEntity<List<RecurringTransactionResponseDto>> getRecurringTransactions(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        List<RecurringTransactionResponseDto> response = recurringTransactionService.getRecurringTransactions(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "반복 거래 비활성화", description = "반복 거래를 비활성화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비활성화 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 반복 거래를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/recurring/{recurringTransactionId}")
    public ResponseEntity<Void> deactivateRecurringTransaction(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "반복 거래 ID", required = true) @PathVariable("recurringTransactionId") Long recurringTransactionId,
            HttpSession session) {
        recurringTransactionService.deactivateRecurringTransaction(session, accountBookId, recurringTransactionId);
        return ResponseEntity.ok().build();
    }
}

