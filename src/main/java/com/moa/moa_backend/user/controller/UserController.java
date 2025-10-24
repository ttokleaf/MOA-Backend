package com.moa.moa_backend.user.controller;

import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.dto.UpdateNicknameRequestDto;
import com.moa.moa_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "닉네임 변경", description = "현재 로그인된 사용자의 닉네임을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PutMapping("/nickname")
    public ResponseEntity<User> updateNickname(
            @Valid @RequestBody UpdateNicknameRequestDto request,
            HttpSession session) {
        User updatedUser = userService.updateNickname(session, request.getNickname());
        return ResponseEntity.ok(updatedUser);
    }

}
