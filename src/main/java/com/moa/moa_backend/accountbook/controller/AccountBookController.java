package com.moa.moa_backend.accountbook.controller;

import com.moa.moa_backend.accountbook.dto.*;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountbooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AccountBook", description = "가계부 관리 API")
public class AccountBookController {

    private final AccountBookService accountBookService;
    private final AuthService authService;

    @Operation(summary = "가계부 생성", description = "새로운 가계부를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "가계부 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<AccountBookResponseDto> createAccountBook(
            @Valid @RequestBody AccountBookCreateRequestDto requestDto,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AccountBookResponseDto responseDto = accountBookService.createAccountBook(requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "내 가계부 목록 조회", description = "현재 사용자가 참여한 가계부 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<List<AccountBookResponseDto>> getMyAccountBooks(HttpSession session) {
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(user);
        return ResponseEntity.ok(accountBooks);
    }

    @Operation(summary = "가계부 상세 조회", description = "특정 가계부의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountBookResponseDto> getAccountBook(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AccountBookResponseDto responseDto = accountBookService.getAccountBook(id, user);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "가계부 수정", description = "기존 가계부의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AccountBookResponseDto> updateAccountBook(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody AccountBookUpdateRequestDto requestDto,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AccountBookResponseDto responseDto = accountBookService.updateAccountBook(id, requestDto, user);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "가계부 삭제", description = "가계부를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountBook(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            accountBookService.deleteAccountBook(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "가계부 참여", description = "참여 코드를 사용하여 가계부에 참여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "400", description = "잘못된 참여 코드")
    })
    @PostMapping("/join")
    public ResponseEntity<AccountBookResponseDto> joinAccountBook(
            @Valid @RequestBody AccountBookJoinRequestDto requestDto,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AccountBookResponseDto responseDto = accountBookService.joinAccountBook(requestDto, user);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "사용자 추방", description = "가계부에서 특정 사용자를 추방합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "추방 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<Void> removeUserFromAccountBook(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "사용자 ID", required = true) @PathVariable("userId") Long userId,
            HttpSession session) {
        
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            accountBookService.removeUserFromAccountBook(id, userId, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
