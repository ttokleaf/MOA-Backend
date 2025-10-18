package com.moa.moa_backend.accountbook.dto;

import com.moa.moa_backend.accountbook.domain.AccountBookRole;
import com.moa.moa_backend.user.domain.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBookUserResponseDto {

    private Long userId;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private AccountBookRole role;
    private LocalDateTime joinedAt;

    // User 엔티티와 권한을 DTO로 변환
    public static AccountBookUserResponseDto from(User user, AccountBookRole role, LocalDateTime joinedAt) {
        return AccountBookUserResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(role)
                .joinedAt(joinedAt)
                .build();
    }
}
