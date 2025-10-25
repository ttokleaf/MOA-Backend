package com.moa.moa_backend.recurring.repository;

import com.moa.moa_backend.recurring.domain.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    /**
     * 활성화된 반복 거래 중에서 아직 생성되지 않은 거래들을 조회
     */
    @Query("SELECT rt FROM RecurringTransaction rt " +
           "WHERE rt.isActive = true " +
           "AND rt.endDate >= :currentDate " +
           "AND (rt.lastGeneratedDate IS NULL OR rt.lastGeneratedDate < :currentDate)")
    List<RecurringTransaction> findActiveRecurringTransactionsToGenerate(@Param("currentDate") LocalDate currentDate);

    /**
     * 특정 가계부의 활성화된 반복 거래 조회
     */
    @Query("SELECT rt FROM RecurringTransaction rt " +
           "WHERE rt.accountBook.id = :accountBookId " +
           "AND rt.isActive = true " +
           "ORDER BY rt.createdAt DESC")
    List<RecurringTransaction> findByAccountBookIdAndActive(@Param("accountBookId") Long accountBookId);

    /**
     * 특정 사용자가 생성한 반복 거래 조회
     */
    @Query("SELECT rt FROM RecurringTransaction rt " +
           "WHERE rt.createdBy.id = :userId " +
           "AND rt.isActive = true " +
           "ORDER BY rt.createdAt DESC")
    List<RecurringTransaction> findByCreatedByAndActive(@Param("userId") Long userId);
}
