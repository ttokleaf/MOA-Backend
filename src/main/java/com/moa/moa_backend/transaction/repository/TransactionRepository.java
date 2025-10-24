package com.moa.moa_backend.transaction.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.category.domain.Category;
import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.transaction.domain.TransactionType;
import com.moa.moa_backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 가계부의 활성화된 거래 내역을 거래일시 내림차순으로 조회
     */
    List<Transaction> findByAccountBookAndIsActiveTrueOrderByTransactionDateDesc(AccountBook accountBook);

    /**
     * 가계부의 모든 거래 내역을 거래일시 내림차순으로 조회
     */
    List<Transaction> findByAccountBookOrderByTransactionDateDesc(AccountBook accountBook);

    /**
     * 가계부에서 특정 ID의 거래 내역 조회
     */
    Optional<Transaction> findByIdAndAccountBook(Long id, AccountBook accountBook);

    /**
     * 가계부의 활성화된 거래 내역 개수 조회
     */
    long countByAccountBookAndIsActiveTrue(AccountBook accountBook);

    /**
     * 가계부에서 특정 ID의 거래 내역을 생성자와 함께 조회
     */
    Optional<Transaction> findByIdAndAccountBookAndCreatedBy(Long id, AccountBook accountBook, User createdBy);

    /**
     * 가계부의 특정 기간 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndIsActiveTrueAndTransactionDateBetweenOrderByTransactionDateDesc(
            AccountBook accountBook, LocalDate startDate, LocalDate endDate);

    /**
     * 가계부의 특정 월 거래 내역 조회
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountBook = :accountBook AND t.isActive = true " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountBookAndYearAndMonth(
            @Param("accountBook") AccountBook accountBook, 
            @Param("year") int year, 
            @Param("month") int month);

    /**
     * 가계부의 특정 날짜 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndIsActiveTrueAndTransactionDateOrderByCreatedAtDesc(
            AccountBook accountBook, LocalDate transactionDate);

    /**
     * 카테고리별 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndCategoryAndIsActiveTrueOrderByTransactionDateDesc(
            AccountBook accountBook, Category category);

    /**
     * 결제 수단별 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndPaymentMethodAndIsActiveTrueOrderByTransactionDateDesc(
            AccountBook accountBook, PaymentMethod paymentMethod);

    /**
     * 거래 타입별 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndTypeAndIsActiveTrueOrderByTransactionDateDesc(
            AccountBook accountBook, TransactionType type);

    /**
     * 반복 거래 내역 조회
     */
    List<Transaction> findByAccountBookAndRecurringTypeNotAndIsActiveTrueOrderByTransactionDateDesc(
            AccountBook accountBook, RecurringType recurringType);

    /**
     * 가계부의 총 수입 금액 계산
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'INCOME' AND t.isActive = true")
    Long getTotalIncomeByAccountBook(@Param("accountBook") AccountBook accountBook);

    /**
     * 가계부의 총 지출 금액 계산
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'EXPENSE' AND t.isActive = true")
    Long getTotalExpenseByAccountBook(@Param("accountBook") AccountBook accountBook);

    /**
     * 가계부의 총 저축 금액 계산
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'SAVINGS' AND t.isActive = true")
    Long getTotalSavingsByAccountBook(@Param("accountBook") AccountBook accountBook);

    /**
     * 특정 기간의 거래 내역 통계
     */
    @Query("SELECT t.type, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.isActive = true AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.type")
    List<Object[]> getTransactionStatsByDateRange(
            @Param("accountBook") AccountBook accountBook,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 페이징된 거래 내역 조회
     */
    Page<Transaction> findByAccountBookAndIsActiveTrueOrderByTransactionDateDesc(
            AccountBook accountBook, Pageable pageable);

    /**
     * 특정 기간의 총 수입 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'INCOME' AND t.isActive = true AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long getTotalIncomeByDateRange(@Param("accountBook") AccountBook accountBook,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간의 총 지출 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'EXPENSE' AND t.isActive = true AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long getTotalExpenseByDateRange(@Param("accountBook") AccountBook accountBook,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간의 예산에서 제외된 총 합계 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.isExcludedFromBudget = true AND t.isActive = true AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long getTotalExcludedFromBudgetByDateRange(@Param("accountBook") AccountBook accountBook,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 특정 월의 총 수입 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'INCOME' AND t.isActive = true " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    Long getTotalIncomeByMonth(@Param("accountBook") AccountBook accountBook,
                               @Param("year") int year,
                               @Param("month") int month);

    /**
     * 특정 월의 총 지출 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.type = 'EXPENSE' AND t.isActive = true " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    Long getTotalExpenseByMonth(@Param("accountBook") AccountBook accountBook,
                                @Param("year") int year,
                                @Param("month") int month);

    /**
     * 특정 월의 예산에서 제외된 총 합계 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.accountBook = :accountBook " +
           "AND t.isExcludedFromBudget = true AND t.isActive = true " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    Long getTotalExcludedFromBudgetByMonth(@Param("accountBook") AccountBook accountBook,
                                           @Param("year") int year,
                                           @Param("month") int month);

    /**
     * 특정 날짜의 거래 내역 조회 (프로필 정보 포함)
     */
    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.createdBy " +
           "JOIN FETCH t.category " +
           "JOIN FETCH t.paymentMethod " +
           "WHERE t.accountBook = :accountBook AND t.isActive = true AND t.transactionDate = :date " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountBookAndDateWithDetails(@Param("accountBook") AccountBook accountBook,
                                                          @Param("date") LocalDate date);

    /**
     * 특정 월의 일별 통계 조회
     */
    @Query("SELECT t.transactionDate, t.type, COALESCE(SUM(t.amount), 0), " +
           "COALESCE(SUM(CASE WHEN t.isExcludedFromBudget = true THEN t.amount ELSE 0 END), 0) " +
           "FROM Transaction t WHERE t.accountBook = :accountBook AND t.isActive = true " +
           "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
           "GROUP BY t.transactionDate, t.type")
    List<Object[]> getDailyStatsByMonth(@Param("accountBook") AccountBook accountBook,
                                        @Param("year") int year,
                                        @Param("month") int month);
}

