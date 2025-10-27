package com.moa.moa_backend.budget.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * 특정 가계부의 특정 년월 예산 조회
     */
    Optional<Budget> findByAccountBookAndYearAndMonthAndIsActiveTrue(
            AccountBook accountBook, Integer year, Integer month);

    /**
     * 특정 가계부의 모든 예산 조회
     */
    List<Budget> findByAccountBookAndIsActiveTrueOrderByYearDescMonthDesc(AccountBook accountBook);

    /**
     * 특정 가계부의 특정 기간 예산 조회
     */
    @Query("SELECT b FROM Budget b WHERE b.accountBook = :accountBook AND b.isActive = true " +
           "AND (b.year > :startYear OR (b.year = :startYear AND b.month >= :startMonth)) " +
           "AND (b.year < :endYear OR (b.year = :endYear AND b.month <= :endMonth)) " +
           "ORDER BY b.year DESC, b.month DESC")
    List<Budget> findByAccountBookAndDateRange(
            @Param("accountBook") AccountBook accountBook,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth);

    /**
     * 특정 가계부의 특정 년월 예산 존재 여부 확인
     */
    boolean existsByAccountBookAndYearAndMonthAndIsActiveTrue(
            AccountBook accountBook, Integer year, Integer month);

    /**
     * 특정 가계부의 최근 예산 조회 (최신 순)
     */
    @Query("SELECT b FROM Budget b WHERE b.accountBook = :accountBook AND b.isActive = true " +
           "ORDER BY b.year DESC, b.month DESC")
    List<Budget> findRecentBudgetsByAccountBook(@Param("accountBook") AccountBook accountBook);
}
