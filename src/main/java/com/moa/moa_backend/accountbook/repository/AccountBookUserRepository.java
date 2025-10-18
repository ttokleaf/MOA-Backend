package com.moa.moa_backend.accountbook.repository;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.domain.AccountBookUser;
import com.moa.moa_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookUserRepository extends JpaRepository<AccountBookUser, Long> {

    // 가계부와 사용자로 관계 찾기
    Optional<AccountBookUser> findByAccountBookAndUser(AccountBook accountBook, User user);

    // 가계부에 참여한 모든 사용자 조회
    List<AccountBookUser> findByAccountBookOrderByJoinedAtAsc(AccountBook accountBook);

    // 사용자가 참여한 모든 가계부 조회
    List<AccountBookUser> findByUserOrderByJoinedAtDesc(User user);

    // 가계부에 사용자가 참여했는지 확인
    boolean existsByAccountBookAndUser(AccountBook accountBook, User user);

    // 가계부의 방장 조회
    @Query("SELECT abu FROM AccountBookUser abu " +
           "WHERE abu.accountBook = :accountBook AND abu.role = 'OWNER'")
    Optional<AccountBookUser> findOwnerByAccountBook(@Param("accountBook") AccountBook accountBook);

    // 가계부의 멤버 수 조회
    @Query("SELECT COUNT(abu) FROM AccountBookUser abu " +
           "WHERE abu.accountBook = :accountBook")
    long countByAccountBook(@Param("accountBook") AccountBook accountBook);

    // 가계부에서 사용자 삭제
    void deleteByAccountBookAndUser(AccountBook accountBook, User user);
}
