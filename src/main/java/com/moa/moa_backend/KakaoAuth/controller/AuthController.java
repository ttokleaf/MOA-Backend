package com.moa.moa_backend.KakaoAuth.controller;

import com.moa.moa_backend.KakaoAuth.domain.User;
import com.moa.moa_backend.KakaoAuth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 프론트 콜백에서 code를 받아 호출: /api/auth/kakao-token?code=...
    @GetMapping("/kakao-token")
    public User handleKakaoCallback(@RequestParam String code, HttpSession session) {
        // 로그인 처리 + 세션 수립 (JSESSIONID가 Set-Cookie로 내려감)
        return authService.loginWithKakaoCode(code, session);
    }

    // 현재 로그인 사용자 조회 (세션 필요)
    @GetMapping("/me")
    public User me(HttpSession session) {
        User user = authService.currentUser(session);
        if (user == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return user;
    }

    // 로그아웃 (세션 무효화)
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }
}
