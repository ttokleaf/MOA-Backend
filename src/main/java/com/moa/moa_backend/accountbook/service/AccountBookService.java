package com.moa.moa_backend.accountbook.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.domain.AccountBookRole;
import com.moa.moa_backend.accountbook.domain.AccountBookUser;
import com.moa.moa_backend.accountbook.dto.*;
import com.moa.moa_backend.accountbook.repository.AccountBookRepository;
import com.moa.moa_backend.accountbook.repository.AccountBookUserRepository;
import com.moa.moa_backend.category.service.CategoryService;
import com.moa.moa_backend.paymentmethod.service.PaymentMethodService;
import com.moa.moa_backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final AccountBookUserRepository accountBookUserRepository;
    private final PaymentMethodService paymentMethodService;
    private final CategoryService categoryService;

    /**
     * 사용자가 접근 가능한 가계부를 조회합니다.
     */
    public AccountBook getAccountBookByIdAndUser(Long accountBookId, User user) {
        // 가계부 존재 확인
        AccountBook accountBook = accountBookRepository.findById(accountBookId)
                .orElse(null);
        
        if (accountBook == null) {
            return null;
        }

        // 사용자가 해당 가계부에 참여하고 있는지 확인
        boolean isParticipant = accountBookUserRepository.existsByAccountBookAndUser(accountBook, user);
        
        if (!isParticipant) {
            return null;
        }

        return accountBook;
    }

    // 가계부 생성
    @Transactional
    public AccountBookResponseDto createAccountBook(AccountBookCreateRequestDto requestDto, User user) {
        // 참여 코드 생성 (UUID)
        String participationCode = generateUniqueParticipationCode();

        // 가계부 생성
        AccountBook accountBook = AccountBook.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .profileImageUrl(requestDto.getProfileImageUrl())
                .participationCode(participationCode)
                .build();

        // 생성자를 방장으로 추가
        accountBook.addOwner(user);

        // 저장
        AccountBook savedAccountBook = accountBookRepository.save(accountBook);

        // 기본 결제 수단 생성
        try {
            paymentMethodService.createDefaultPaymentMethods(savedAccountBook, user);
            log.info("가계부 {}의 기본 결제 수단 생성 완료", savedAccountBook.getId());
        } catch (Exception e) {
            log.error("가계부 {}의 기본 결제 수단 생성 실패: {}", savedAccountBook.getId(), e.getMessage());
        }

        // 기본 카테고리 생성
        try {
            categoryService.createDefaultCategories(savedAccountBook, user);
            log.info("가계부 {}의 기본 카테고리 생성 완료", savedAccountBook.getId());
        } catch (Exception e) {
            log.error("가계부 {}의 기본 카테고리 생성 실패: {}", savedAccountBook.getId(), e.getMessage());
        }

        log.info("가계부 생성 완료: ID={}, 이름={}, 생성자={}", 
                savedAccountBook.getId(), savedAccountBook.getName(), user.getNickname());

        return AccountBookResponseDto.from(savedAccountBook, AccountBookRole.OWNER);
    }

    // 사용자의 가계부 목록 조회
    public List<AccountBookResponseDto> getAccountBooksByUser(User user) {
        List<AccountBook> accountBooks = accountBookRepository.findByUser(user);
        
        return accountBooks.stream()
                .map(accountBook -> {
                    AccountBookRole userRole = accountBook.getUserRole(user);
                    return AccountBookResponseDto.from(accountBook, userRole);
                })
                .collect(Collectors.toList());
    }

    // 가계부 상세 조회
    public AccountBookResponseDto getAccountBook(Long accountBookId, User user) {
        AccountBook accountBook = findAccountBookById(accountBookId);
        
        // 사용자가 가계부에 참여했는지 확인
        if (!accountBook.hasUser(user)) {
            throw new IllegalArgumentException("가계부에 접근할 권한이 없습니다.");
        }

        AccountBookRole userRole = accountBook.getUserRole(user);
        
        // 가계부 참여자 목록 조회
        List<AccountBookUserResponseDto> users = getAccountBookUsers(accountBook);

        return AccountBookResponseDto.fromWithUsers(accountBook, userRole, users);
    }

    // 가계부 수정
    @Transactional
    public AccountBookResponseDto updateAccountBook(Long accountBookId, AccountBookUpdateRequestDto requestDto, User user) {
        AccountBook accountBook = findAccountBookById(accountBookId);
        
        // 방장만 수정 가능
        if (!accountBook.isOwner(user)) {
            throw new IllegalArgumentException("가계부를 수정할 권한이 없습니다.");
        }

        // 수정할 필드가 있는 경우만 업데이트
        if (requestDto.getName() != null) {
            accountBook.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            accountBook.setDescription(requestDto.getDescription());
        }
        if (requestDto.getProfileImageUrl() != null) {
            accountBook.setProfileImageUrl(requestDto.getProfileImageUrl());
        }

        AccountBook savedAccountBook = accountBookRepository.save(accountBook);
        AccountBookRole userRole = savedAccountBook.getUserRole(user);

        log.info("가계부 수정 완료: ID={}, 수정자={}", accountBookId, user.getNickname());

        return AccountBookResponseDto.from(savedAccountBook, userRole);
    }

    // 가계부 삭제
    @Transactional
    public void deleteAccountBook(Long accountBookId, User user) {
        AccountBook accountBook = findAccountBookById(accountBookId);
        
        // 방장만 삭제 가능
        if (!accountBook.isOwner(user)) {
            throw new IllegalArgumentException("가계부를 삭제할 권한이 없습니다.");
        }

        accountBookRepository.delete(accountBook);
        log.info("가계부 삭제 완료: ID={}, 삭제자={}", accountBookId, user.getNickname());
    }

    // 참여 코드로 가계부 참여
    @Transactional
    public AccountBookResponseDto joinAccountBook(AccountBookJoinRequestDto requestDto, User user) {
        AccountBook accountBook = accountBookRepository.findByParticipationCode(requestDto.getParticipationCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 참여 코드입니다."));

        // 이미 참여한 가계부인지 확인
        if (accountBook.hasUser(user)) {
            throw new IllegalArgumentException("이미 참여한 가계부입니다.");
        }

        // 멤버로 추가
        accountBook.addMember(user);
        accountBookRepository.save(accountBook);

        log.info("가계부 참여 완료: 가계부 ID={}, 참여자={}", accountBook.getId(), user.getNickname());

        return AccountBookResponseDto.from(accountBook, AccountBookRole.MEMBER);
    }

    // 가계부에서 사용자 추방
    @Transactional
    public void removeUserFromAccountBook(Long accountBookId, Long userId, User requester) {
        AccountBook accountBook = findAccountBookById(accountBookId);
        
        // 방장만 추방 가능
        if (!accountBook.isOwner(requester)) {
            throw new IllegalArgumentException("사용자를 추방할 권한이 없습니다.");
        }

        // 추방할 사용자 찾기
        User userToRemove = accountBook.getAccountBookUsers().stream()
                .map(AccountBookUser::getUser)
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("가계부에 존재하지 않는 사용자입니다."));

        // 방장은 추방할 수 없음
        if (accountBook.isOwner(userToRemove)) {
            throw new IllegalArgumentException("방장은 추방할 수 없습니다.");
        }

        // 사용자 제거
        accountBook.removeUser(userToRemove);
        accountBookRepository.save(accountBook);

        log.info("사용자 추방 완료: 가계부 ID={}, 추방된 사용자={}, 추방자={}", 
                accountBookId, userToRemove.getNickname(), requester.getNickname());
    }

    // 가계부 참여자 목록 조회
    public List<AccountBookUserResponseDto> getAccountBookUsers(AccountBook accountBook) {
        List<AccountBookUser> accountBookUsers = accountBookUserRepository.findByAccountBookOrderByJoinedAtAsc(accountBook);
        
        return accountBookUsers.stream()
                .map(accountBookUser -> AccountBookUserResponseDto.from(
                        accountBookUser.getUser(),
                        accountBookUser.getRole(),
                        accountBookUser.getJoinedAt()
                ))
                .collect(Collectors.toList());
    }

    // 가계부 ID로 조회 (내부 메서드)
    private AccountBook findAccountBookById(Long accountBookId) {
        return accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가계부입니다."));
    }

    // 고유한 참여 코드 생성
    private String generateUniqueParticipationCode() {
        String participationCode;
        do {
            participationCode = UUID.randomUUID().toString();
        } while (accountBookRepository.existsByParticipationCode(participationCode));
        
        return participationCode;
    }
}
