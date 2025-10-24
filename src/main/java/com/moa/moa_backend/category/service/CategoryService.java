package com.moa.moa_backend.category.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.category.domain.Category;
import com.moa.moa_backend.category.dto.CategoryResponseDto;
import com.moa.moa_backend.category.dto.CreateCategoryRequestDto;
import com.moa.moa_backend.category.repository.CategoryRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final AccountBookService accountBookService;

    public CategoryService(CategoryRepository categoryRepository, 
                           UserService userService, 
                           @Lazy AccountBookService accountBookService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.accountBookService = accountBookService;
    }

    @Transactional
    public CategoryResponseDto createCategory(HttpSession session, Long accountBookId, CreateCategoryRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 중복 이름 확인
        if (categoryRepository.existsByAccountBookAndNameAndIsActiveTrue(accountBook, request.getName())) {
            throw new RuntimeException("이미 존재하는 카테고리 이름입니다.");
        }

        Category category = Category.builder()
                .name(request.getName())
                .accountBook(accountBook)
                .createdBy(currentUser)
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        
        log.info("카테고리 생성 완료 - 가계부 ID: {}, 사용자 ID: {}, 카테고리 ID: {}, 이름: {}", 
                accountBookId, currentUser.getId(), savedCategory.getId(), savedCategory.getName());

        return CategoryResponseDto.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getCategories(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Category> categories = categoryRepository.findByAccountBookAndIsActiveTrueOrderByCreatedAtDesc(accountBook);
        
        log.info("카테고리 목록 조회 완료 - 가계부 ID: {}, 사용자 ID: {}, 조회된 카테고리 수: {}", 
                accountBookId, currentUser.getId(), categories.size());

        return categories.stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategory(HttpSession session, Long accountBookId, Long categoryId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Category category = categoryRepository.findByIdAndAccountBook(categoryId, accountBook)
                .orElse(null);
        
        if (category == null || !category.getIsActive()) {
            throw new RuntimeException("카테고리를 찾을 수 없습니다.");
        }

        log.info("카테고리 상세 조회 완료 - 가계부 ID: {}, 사용자 ID: {}, 카테고리 ID: {}", 
                accountBookId, currentUser.getId(), categoryId);

        return CategoryResponseDto.from(category);
    }

    @Transactional
    public void deleteCategory(HttpSession session, Long accountBookId, Long categoryId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Category category = categoryRepository.findByIdAndAccountBookAndCreatedBy(categoryId, accountBook, currentUser)
                .orElse(null);
        
        if (category == null || !category.getIsActive()) {
            throw new RuntimeException("카테고리를 찾을 수 없거나 삭제 권한이 없습니다.");
        }

        // 소프트 삭제
        category.setIsActive(false);
        categoryRepository.save(category);
        
        log.info("카테고리 삭제 완료 - 가계부 ID: {}, 사용자 ID: {}, 카테고리 ID: {}, 이름: {}", 
                accountBookId, currentUser.getId(), categoryId, category.getName());
    }

    /**
     * 가계부에 기본 카테고리들을 생성합니다.
     * 식비, 여가, 생필품 기본값을 제공합니다.
     */
    @Transactional
    public void createDefaultCategories(AccountBook accountBook, User createdBy) {
        // 이미 기본 카테고리가 있는지 확인
        long existingCount = categoryRepository.countByAccountBookAndIsActiveTrue(accountBook);
        if (existingCount > 0) {
            log.info("가계부 {}는 이미 카테고리가 존재합니다. 기본 카테고리 생성을 건너뜁니다.", accountBook.getId());
            return;
        }

        // 기본 카테고리 정의
        List<String> defaultCategories = Arrays.asList("식비", "여가", "생필품");

        // 기본 카테고리들 생성
        for (String categoryName : defaultCategories) {
            Category category = Category.builder()
                    .name(categoryName)
                    .accountBook(accountBook)
                    .createdBy(createdBy)
                    .isActive(true)
                    .build();

            categoryRepository.save(category);
            log.info("기본 카테고리 생성 완료 - 가계부 ID: {}, 카테고리: {}", accountBook.getId(), categoryName);
        }

        log.info("가계부 {}의 기본 카테고리 생성 완료", accountBook.getId());
    }
}
