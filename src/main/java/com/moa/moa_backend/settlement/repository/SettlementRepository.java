package com.moa.moa_backend.settlement.repository;

import com.moa.moa_backend.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // 가계부별 정산 목록 조회
    @Query("SELECT s FROM Settlement s WHERE s.accountBook.id = :accountBookId AND s.isActive = true ORDER BY s.createdAt DESC")
    List<Settlement> findByAccountBookIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("accountBookId") Long accountBookId);

    // 공유 코드로 정산 조회
    Optional<Settlement> findByShareCodeAndIsActiveTrue(String shareCode);

    // 사용자가 참여한 정산 목록 조회
    @Query("SELECT DISTINCT s FROM Settlement s " +
           "JOIN s.participants p " +
           "WHERE p.user.id = :userId AND s.isActive = true " +
           "ORDER BY s.createdAt DESC")
    List<Settlement> findSettlementsByParticipantUserId(@Param("userId") Long userId);

    // 가계부의 특정 기간 정산 존재 여부 확인
    @Query("SELECT COUNT(s) > 0 FROM Settlement s " +
           "WHERE s.accountBook.id = :accountBookId " +
           "AND s.isActive = true " +
           "AND ((s.startDate <= :endDate AND s.endDate >= :startDate))")
    boolean existsOverlappingSettlement(@Param("accountBookId") Long accountBookId, 
                                       @Param("startDate") java.time.LocalDate startDate, 
                                       @Param("endDate") java.time.LocalDate endDate);
}
