package com.moa.moa_backend.accountbook.controller;

import com.moa.moa_backend.accountbook.dto.*;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.AuthService;
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
public class AccountBookController {

    private final AccountBookService accountBookService;
    private final AuthService authService;

    // 가계부 생성
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

    // 내 가계부 목록 조회
    @GetMapping
    public ResponseEntity<List<AccountBookResponseDto>> getMyAccountBooks(HttpSession session) {
        User user = authService.currentUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(user);
        return ResponseEntity.ok(accountBooks);
    }

    // 가계부 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AccountBookResponseDto> getAccountBook(
            @PathVariable Long id,
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

    // 가계부 수정
    @PutMapping("/{id}")
    public ResponseEntity<AccountBookResponseDto> updateAccountBook(
            @PathVariable Long id,
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

    // 가계부 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountBook(
            @PathVariable Long id,
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

    // 참여 코드로 가계부 참여
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

    // 가계부에서 사용자 추방
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<Void> removeUserFromAccountBook(
            @PathVariable Long id,
            @PathVariable Long userId,
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
