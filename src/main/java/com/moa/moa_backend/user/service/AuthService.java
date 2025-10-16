package com.moa.moa_backend.KakaoAuth.service;

import com.moa.moa_backend.KakaoAuth.domain.User;
import com.moa.moa_backend.KakaoAuth.dto.KakaoTokenResponseDto;
import com.moa.moa_backend.KakaoAuth.dto.KakaoUserResponseDto;
import com.moa.moa_backend.KakaoAuth.repository.UserRepository;
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

    public User currentUser(HttpSession session) {
        Object id = session.getAttribute("LOGIN_USER_ID");
        if (id == null) return null;
        return userRepository.findById((Long) id).orElse(null);
    }
}
