package com.moa.moa_backend.KakaoAuth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserResponseDto {
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;

    @Data
    public static class KakaoAccount {
        private Profile profile;
        private String email;

        @Data
        public static class Profile {
            private String nickname;
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }

    @Data
    public static class Properties {
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}
