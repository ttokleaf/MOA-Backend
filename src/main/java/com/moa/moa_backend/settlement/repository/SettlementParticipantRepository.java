package com.moa.moa_backend.settlement.repository;

import com.moa.moa_backend.settlement.domain.SettlementParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementParticipantRepository extends JpaRepository<SettlementParticipant, Long> {

    // 정산별 참여자 목록 조회
    List<SettlementParticipant> findBySettlementIdAndIsActiveTrue(Long settlementId);

    // 사용자별 정산 참여 내역 조회
    @Query("SELECT sp FROM SettlementParticipant sp " +
           "WHERE sp.user.id = :userId AND sp.isActive = true " +
           "ORDER BY sp.settlement.createdAt DESC")
    List<SettlementParticipant> findByUserIdAndIsActiveTrueOrderBySettlementCreatedAtDesc(@Param("userId") Long userId);

    // 정산과 사용자로 참여자 조회
    Optional<SettlementParticipant> findBySettlementIdAndUserIdAndIsActiveTrue(Long settlementId, Long userId);

    // 정산별 미결제 참여자 수 조회
    @Query("SELECT COUNT(sp) FROM SettlementParticipant sp " +
           "WHERE sp.settlement.id = :settlementId AND sp.isPaid = false AND sp.isActive = true")
    long countUnpaidParticipantsBySettlementId(@Param("settlementId") Long settlementId);
}
