package com.moa.moa_backend.user.service;

import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.dto.KakaoTokenResponseDto;
import com.moa.moa_backend.user.dto.KakaoUserResponseDto;
import com.moa.moa_backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoService kakaoService;
    private final UserRepository userRepository;

    public User loginWithKakaoCode(String code, HttpSession session) {
        // 1) code -> access/refresh token
        KakaoTokenResponseDto token = kakaoService.exchangeCodeForToken(code);

        // 2) access token으로 사용자 정보 조회
        KakaoUserResponseDto profile = kakaoService.getUserProfile(token.getAccessToken());

        Long kakaoId = profile.getId();
        String nickname = (profile.getKakaoAccount() != null && profile.getKakaoAccount().getProfile() != null)
                ? profile.getKakaoAccount().getProfile().getNickname()
                : (profile.getProperties() != null ? profile.getProperties().getNickname() : null);
        String email = profile.getKakaoAccount() != null ? profile.getKakaoAccount().getEmail() : null;
        String img = (profile.getKakaoAccount() != null && profile.getKakaoAccount().getProfile() != null)
                ? profile.getKakaoAccount().getProfile().getProfileImageUrl()
                : (profile.getProperties() != null ? profile.getProperties().getProfileImage() : null);

        // 3) 우리 서비스 사용자 매핑(없으면 생성)
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .email(email)
                        .profileImageUrl(img)
                        .build()));

        // 4) 세션에 로그인 정보 및 카카오 토큰 저장(필요시)
        session.setAttribute("LOGIN_USER_ID", user.getId());
        session.setAttribute("KAKAO_ACCESS_TOKEN", token.getAccessToken());
        session.setAttribute("KAKAO_REFRESH_TOKEN", token.getRefreshToken());

        log.info("[DEBUG] 세션에 저장된 LOGIN_USER_ID = {}", session.getAttribute("LOGIN_USER_ID"));
        log.info("[DEBUG] 세션 ID = {}", session.getId());


        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);

        // SecurityContext 생성 및 주입
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 중요: SecurityContext를 세션에도 저장해야, 다음 요청에서 인증이 유지됩니다.
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public void logoutWithoutInvalidate(HttpSession session) {
        // 세션을 무효화하지 않고 로그아웃만 처리
        session.removeAttribute("LOGIN_USER_ID");
        session.removeAttribute("KAKAO_ACCESS_TOKEN");
        session.removeAttribute("KAKAO_REFRESH_TOKEN");
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    public User currentUser(HttpSession session) {
        Object id = session.getAttribute("LOGIN_USER_ID");
        if (id == null) return null;
        return userRepository.findById((Long) id).orElse(null);
    }

    public User createOrGetTestUser() {
        // 테스트용 카카오 ID (실제 카카오 ID와 충돌하지 않도록 큰 값 사용)
        Long testKakaoId = 999999999L;
        
        return userRepository.findByKakaoId(testKakaoId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(testKakaoId)
                        .nickname("테스트사용자")
                        .email("test@example.com")
                        .profileImageUrl("https://via.placeholder.com/150")
                        .build()));
    }

    public User createOrGetTestUser2() {
        // 두 번째 테스트용 카카오 ID
        Long testKakaoId2 = 999999998L;
        
        return userRepository.findByKakaoId(testKakaoId2)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(testKakaoId2)
                        .nickname("테스트사용자2")
                        .email("test2@example.com")
                        .profileImageUrl("https://via.placeholder.com/150")
                        .build()));
    }


    public User loginAsTestUserWithNewSession(HttpSession session) {
        // 테스트용 사용자 생성 또는 조회
        User testUser = createOrGetTestUser();
        
        // 새로운 세션에 로그인 정보 저장
        session.setAttribute("LOGIN_USER_ID", testUser.getId());
        
        // Spring Security 인증 설정
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser.getId(), null, authorities);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        
        log.info("[DEBUG] 새 세션으로 테스트 사용자1 로그인 - 세션 ID: {}, 사용자 ID: {}", session.getId(), testUser.getId());
        
        return testUser;
    }

    public User loginAsTestUser2WithNewSession(HttpSession session) {
        // 두 번째 테스트용 사용자 생성 또는 조회
        User testUser = createOrGetTestUser2();
        
        // 새로운 세션에 로그인 정보 저장
        session.setAttribute("LOGIN_USER_ID", testUser.getId());
        
        // Spring Security 인증 설정
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser.getId(), null, authorities);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        
        log.info("[DEBUG] 새 세션으로 테스트 사용자2 로그인 - 세션 ID: {}, 사용자 ID: {}", session.getId(), testUser.getId());
        
        return testUser;
    }

}
