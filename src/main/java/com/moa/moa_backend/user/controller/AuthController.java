package com.moa.moa_backend.user.controller;

import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @Parameter(description = "카카오 인증 코드", required = true) @RequestParam("code") String code, 
            HttpSession session) {
        // 로그인 처리 + 세션 수립 (JSESSIONID가 Set-Cookie로 내려감)
        return authService.loginWithKakaoCode(code, session);
    }

    // @Operation(summary = "카카오 로그인 콜백 (카카오 리다이렉트용)", description = "카카오 OAuth 리다이렉트 URI에서 호출되는 콜백 엔드포인트입니다.")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "로그인 성공"),
    //         @ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
    // })
    // @GetMapping("/login/kakao")
    // public User handleKakaoRedirectCallback(
    //         @Parameter(description = "카카오 인증 코드", required = true) @RequestParam String code, 
    //         HttpSession session) {
    //     // 로그인 처리 + 세션 수립 (JSESSIONID가 Set-Cookie로 내려감)
    //     return authService.loginWithKakaoCode(code, session);
    // }

    @Operation(summary = "현재 사용자 조회", description = "현재 로그인된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @SecurityRequirement(name = "cookieAuth")
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
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }


    @Operation(summary = "테스트 사용자1 로그인", description = "기존 세션을 무효화하고 새로운 세션으로 테스트 사용자1에 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/test-login")
    public User testLogin(HttpServletRequest request) {
        // 기존 세션 무효화
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        
        // 새로운 세션 강제 생성
        HttpSession newSession = request.getSession(true);
        
        return authService.loginAsTestUserWithNewSession(newSession);
    }

    @Operation(summary = "테스트 사용자2 로그인", description = "기존 세션을 무효화하고 새로운 세션으로 테스트 사용자2에 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/test-login-user2")
    public User testLoginUser2(HttpServletRequest request) {
        // 기존 세션 무효화
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        
        // 새로운 세션 강제 생성
        HttpSession newSession = request.getSession(true);
        
        return authService.loginAsTestUser2WithNewSession(newSession);
    }

    @Operation(summary = "테스트 환경 초기화", description = "테스트용 사용자들과 가계부를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초기화 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/test-setup")
    public ResponseEntity<String> testSetup() {
        try {
            // 테스트용 사용자들 생성
            authService.createOrGetTestUser();
            authService.createOrGetTestUser2();
            
            return ResponseEntity.ok("테스트 환경이 초기화되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("테스트 환경 초기화 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "세션 정보 조회", description = "현재 세션의 상세 정보를 조회합니다. (디버깅용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/session-info")
    public ResponseEntity<String> getSessionInfo(HttpSession session) {
        StringBuilder info = new StringBuilder();
        info.append("세션 ID: ").append(session.getId()).append("\n");
        info.append("생성 시간: ").append(session.getCreationTime()).append("\n");
        info.append("마지막 접근 시간: ").append(session.getLastAccessedTime()).append("\n");
        info.append("최대 비활성 간격: ").append(session.getMaxInactiveInterval()).append("\n");
        
        Object userId = session.getAttribute("LOGIN_USER_ID");
        info.append("로그인 사용자 ID: ").append(userId != null ? userId : "없음").append("\n");
        
        return ResponseEntity.ok(info.toString());
    }
}
