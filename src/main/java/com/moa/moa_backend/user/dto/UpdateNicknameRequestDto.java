package com.moa.moa_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "닉네임 변경 요청")
public class UpdateNicknameRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    @Schema(description = "새로운 닉네임", example = "새로운닉네임", maxLength = 10)
    private String nickname;
}
