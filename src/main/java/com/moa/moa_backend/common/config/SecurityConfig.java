package com.moa.moa_backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${app.frontend-origin}")
    private String frontendOrigin;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 세션 기반이므로 기본 세션 전략 사용
                .csrf(csrf -> csrf.disable()) // SPA-Cookie 조합 시 CSRF를 별도 처리 권장(데모용 disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login/**", "/api/auth/kakao-token", "/api/auth/login/kakao/**", "/api/auth/test-login", "/api/auth/test-login-user2", "/api/auth/test-setup", "/api/auth/session-info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(h -> h
                        .frameOptions(f -> f.disable()) // H2 콘솔용
                        .contentTypeOptions(cto -> cto.disable()) // OAuth 콜백용
                        .httpStrictTransportSecurity(hsts -> hsts.disable()) // 개발환경용
                );

        return http.build();
    }
}
