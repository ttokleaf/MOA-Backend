package com.moa.moa_backend.settlement.repository;

import com.moa.moa_backend.settlement.domain.SettlementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {

    // 정산별 거래 내역 조회
    List<SettlementTransaction> findBySettlementIdOrderById(Long settlementId);

    // 거래 ID로 정산 거래 내역 조회
    @Query("SELECT st FROM SettlementTransaction st WHERE st.transaction.id = :transactionId")
    List<SettlementTransaction> findByTransactionId(@Param("transactionId") Long transactionId);

    // 정산별 총 금액 계산
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SettlementTransaction st WHERE st.settlement.id = :settlementId")
    Long calculateTotalAmountBySettlementId(@Param("settlementId") Long settlementId);
}
