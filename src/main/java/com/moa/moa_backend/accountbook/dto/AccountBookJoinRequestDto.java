package com.moa.moa_backend.accountbook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBookJoinRequestDto {

    @NotBlank(message = "참여 코드는 필수입니다")
    private String participationCode;
}
