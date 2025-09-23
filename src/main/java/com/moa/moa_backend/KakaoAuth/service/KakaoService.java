package com.moa.moa_backend.KakaoAuth.service;

import com.moa.moa_backend.KakaoAuth.dto.KakaoTokenResponseDto;
import com.moa.moa_backend.KakaoAuth.dto.KakaoUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.token-host}")
    private String tokenHost;

    @Value("${kakao.api-host}")
    private String apiHost;

    public KakaoTokenResponseDto exchangeCodeForToken(String code) {
        return webClient.post()
                .uri(tokenHost + "/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        res -> res.bodyToMono(String.class).flatMap(b -> Mono.error(new RuntimeException("Kakao 4xx: " + b))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        res -> res.bodyToMono(String.class).flatMap(b -> Mono.error(new RuntimeException("Kakao 5xx: " + b))))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();
    }

    public KakaoUserResponseDto getUserProfile(String accessToken) {
        return webClient.get()
                .uri(apiHost + "/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        res -> res.bodyToMono(String.class).flatMap(b -> Mono.error(new RuntimeException("Kakao user error: " + b))))
                .bodyToMono(KakaoUserResponseDto.class)
                .block();
    }
}
