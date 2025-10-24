package com.moa.moa_backend.category.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.category.domain.Category;
import com.moa.moa_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 가계부의 활성화된 카테고리 목록을 생성일시 내림차순으로 조회
     */
    List<Category> findByAccountBookAndIsActiveTrueOrderByCreatedAtDesc(AccountBook accountBook);

    /**
     * 가계부의 모든 카테고리 목록을 생성일시 내림차순으로 조회
     */
    List<Category> findByAccountBookOrderByCreatedAtDesc(AccountBook accountBook);

    /**
     * 가계부에서 특정 이름의 활성화된 카테고리가 존재하는지 확인
     */
    boolean existsByAccountBookAndNameAndIsActiveTrue(AccountBook accountBook, String name);

    /**
     * 가계부에서 특정 ID의 카테고리 조회
     */
    Optional<Category> findByIdAndAccountBook(Long id, AccountBook accountBook);

    /**
     * 가계부의 활성화된 카테고리 개수 조회
     */
    long countByAccountBookAndIsActiveTrue(AccountBook accountBook);

    /**
     * 가계부에서 특정 ID의 카테고리를 생성자와 함께 조회
     */
    Optional<Category> findByIdAndAccountBookAndCreatedBy(Long id, AccountBook accountBook, User createdBy);
}
