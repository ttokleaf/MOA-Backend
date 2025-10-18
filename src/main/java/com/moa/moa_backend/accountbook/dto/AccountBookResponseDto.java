package com.moa.moa_backend.accountbook.dto;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.domain.AccountBookRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBookResponseDto {

    private Long id;
    private String name;
    private String description;
    private String profileImageUrl;
    private String participationCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AccountBookRole userRole; // 현재 사용자의 권한
    private List<AccountBookUserResponseDto> users; // 가계부 참여자 목록

    // AccountBook 엔티티를 DTO로 변환
    public static AccountBookResponseDto from(AccountBook accountBook, AccountBookRole userRole) {
        return AccountBookResponseDto.builder()
                .id(accountBook.getId())
                .name(accountBook.getName())
                .description(accountBook.getDescription())
                .profileImageUrl(accountBook.getProfileImageUrl())
                .participationCode(accountBook.getParticipationCode())
                .createdAt(accountBook.getCreatedAt())
                .updatedAt(accountBook.getUpdatedAt())
                .userRole(userRole)
                .build();
    }

    // 사용자 목록과 함께 변환
    public static AccountBookResponseDto fromWithUsers(AccountBook accountBook, AccountBookRole userRole, 
                                                      List<AccountBookUserResponseDto> users) {
        AccountBookResponseDto dto = from(accountBook, userRole);
        dto.setUsers(users);
        return dto;
    }
}
