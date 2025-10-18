package com.moa.moa_backend.accountbook.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBookUpdateRequestDto {

    @Size(max = 100, message = "가계부 이름은 100자를 초과할 수 없습니다")
    private String name;

    @Size(max = 500, message = "가계부 설명은 500자를 초과할 수 없습니다")
    private String description;

    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다")
    private String profileImageUrl;
}
