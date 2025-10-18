package com.moa.moa_backend.user.controller;

import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 콜백을 처리하여 사용자를 로그인시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
    })
    @GetMapping("/kakao-token")
    public User handleKakaoCallback(
            @Parameter(description = "카카오 인증 코드", required = true) @RequestParam String code, 
            HttpSession session) {
        // 로그인 처리 + 세션 수립 (JSESSIONID가 Set-Cookie로 내려감)
        return authService.loginWithKakaoCode(code, session);
    }

    @Operation(summary = "카카오 로그인 콜백 (카카오 리다이렉트용)", description = "카카오 OAuth 리다이렉트 URI에서 호출되는 콜백 엔드포인트입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
    })
    @GetMapping("/login/kakao")
    public User handleKakaoRedirectCallback(
            @Parameter(description = "카카오 인증 코드", required = true) @RequestParam String code, 
            HttpSession session) {
        // 로그인 처리 + 세션 수립 (JSESSIONID가 Set-Cookie로 내려감)
        return authService.loginWithKakaoCode(code, session);
    }

    @Operation(summary = "현재 사용자 조회", description = "현재 로그인된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public User me(HttpSession session) {
        User user = authService.currentUser(session);
        if (user == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return user;
    }

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃하고 세션을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }
}
