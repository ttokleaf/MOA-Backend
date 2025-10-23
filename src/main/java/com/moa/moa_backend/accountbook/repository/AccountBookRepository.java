package com.moa.moa_backend.accountbook.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {

    // 참여 코드로 가계부 찾기
    Optional<AccountBook> findByParticipationCode(String participationCode);

    // 사용자가 참여한 가계부 목록 조회
    @Query("SELECT ab FROM AccountBook ab " +
           "JOIN ab.accountBookUsers abu " +
           "WHERE abu.user = :user " +
           "ORDER BY ab.createdAt DESC")
    List<AccountBook> findByUser(@Param("user") User user);

    // 사용자가 방장인 가계부 목록 조회
    @Query("SELECT ab FROM AccountBook ab " +
           "JOIN ab.accountBookUsers abu " +
           "WHERE abu.user = :user AND abu.role = 'OWNER' " +
           "ORDER BY ab.createdAt DESC")
    List<AccountBook> findOwnedByUser(@Param("user") User user);

    // 사용자가 멤버인 가계부 목록 조회
    @Query("SELECT ab FROM AccountBook ab " +
           "JOIN ab.accountBookUsers abu " +
           "WHERE abu.user = :user AND abu.role = 'MEMBER' " +
           "ORDER BY ab.createdAt DESC")
    List<AccountBook> findMemberByUser(@Param("user") User user);

    // 가계부 이름으로 검색 (사용자가 참여한 가계부 중에서)
    @Query("SELECT ab FROM AccountBook ab " +
           "JOIN ab.accountBookUsers abu " +
           "WHERE abu.user = :user AND ab.name LIKE %:name% " +
           "ORDER BY ab.createdAt DESC")
    List<AccountBook> findByNameContainingAndUser(@Param("name") String name, @Param("user") User user);

    // 참여 코드 존재 여부 확인
    boolean existsByParticipationCode(String participationCode);
}
