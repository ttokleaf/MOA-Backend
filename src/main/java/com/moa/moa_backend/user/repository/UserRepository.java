package com.moa.moa_backend.KakaoAuth.repository;

import com.moa.moa_backend.KakaoAuth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);
}
