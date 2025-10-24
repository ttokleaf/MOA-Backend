package com.moa.moa_backend.user.service;

import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User updateNickname(HttpSession session, String newNickname) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        currentUser.setNickname(newNickname);
        User savedUser = userRepository.save(currentUser);
        
        log.info("사용자 닉네임 변경 완료 - 사용자 ID: {}, 새 닉네임: {}", savedUser.getId(), newNickname);
        
        return savedUser;
    }


    public User getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute("LOGIN_USER_ID");
        if (userId == null) {
            return null;
        }
        return userRepository.findById((Long) userId).orElse(null);
    }
}
