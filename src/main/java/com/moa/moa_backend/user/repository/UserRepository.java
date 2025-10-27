package com.moa.moa_backend.user.repository;

import com.moa.moa_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);
    List<User> findByIdIn(List<Long> ids);
}
