package com.moa.moa_backend.category.controller;

import com.moa.moa_backend.category.dto.CategoryResponseDto;
import com.moa.moa_backend.category.dto.CreateCategoryRequestDto;
import com.moa.moa_backend.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountbooks/{accountBookId}/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 카테고리 이름")
    })
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Valid @RequestBody CreateCategoryRequestDto request,
            HttpSession session) {
        CategoryResponseDto response = categoryService.createCategory(session, accountBookId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리 목록 조회", description = "가계부의 활성화된 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            HttpSession session) {
        List<CategoryResponseDto> response = categoryService.getCategories(session, accountBookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리 상세 조회", description = "특정 카테고리의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 카테고리를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        CategoryResponseDto response = categoryService.getCategory(session, accountBookId, id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리 삭제", description = "기존 카테고리를 삭제합니다. (비활성화 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 카테고리를 찾을 수 없음")
    })
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "가계부 ID", required = true) @PathVariable("accountBookId") Long accountBookId,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        categoryService.deleteCategory(session, accountBookId, id);
        return ResponseEntity.ok().build();
    }
}
