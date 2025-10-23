package com.moa.moa_backend.accountbook.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.domain.AccountBookRole;
import com.moa.moa_backend.accountbook.dto.*;
import com.moa.moa_backend.accountbook.repository.AccountBookRepository;
import com.moa.moa_backend.accountbook.repository.AccountBookUserRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("가계부 비즈니스 로직 테스트")
class AccountBookBusinessLogicTest {

    @Autowired
    private AccountBookService accountBookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    private User owner;
    private User member1;
    private User member2;
    private User outsider;

    @BeforeEach
    void setUp() {
        // 테스트 사용자들 생성
        owner = createUser("방장", 1001L);
        member1 = createUser("멤버1", 1002L);
        member2 = createUser("멤버2", 1003L);
        outsider = createUser("외부인", 1004L);
    }

    private User createUser(String nickname, Long kakaoId) {
        User user = User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .email(nickname + "@example.com")
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("가계부 생성 시 방장이 자동으로 OWNER 권한을 가진다")
    void createAccountBook_OwnerHasOwnerRole() {
        // Given
        AccountBookCreateRequestDto requestDto = AccountBookCreateRequestDto.builder()
                .name("우리 가계부")
                .description("가족용 가계부")
                .build();

        // When
        AccountBookResponseDto result = accountBookService.createAccountBook(requestDto, owner);

        // Then
        assertThat(result.getUserRole()).isEqualTo(AccountBookRole.OWNER);
        assertThat(result.getName()).isEqualTo("우리 가계부");
        
        // 방장이 가계부 목록에 나타나는지 확인
        List<AccountBookResponseDto> ownerAccountBooks = accountBookService.getAccountBooksByUser(owner);
        assertThat(ownerAccountBooks).hasSize(1);
        assertThat(ownerAccountBooks.get(0).getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("참여 코드로 가계부에 참여하면 MEMBER 권한을 가진다")
    void joinAccountBookWithCode_BecomesMember() {
        // Given - 방장이 가계부 생성
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("공유 가계부")
                .description("여러 사람이 함께 사용하는 가계부")
                .build();
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, owner);
        String participationCode = createdAccountBook.getParticipationCode();

        // When - 참여 코드로 참여
        AccountBookJoinRequestDto joinDto = AccountBookJoinRequestDto.builder()
                .participationCode(participationCode)
                .build();
        AccountBookResponseDto joinResult = accountBookService.joinAccountBook(joinDto, member1);

        // Then
        assertThat(joinResult.getUserRole()).isEqualTo(AccountBookRole.MEMBER);
        assertThat(joinResult.getId()).isEqualTo(createdAccountBook.getId());
        
        // 멤버가 가계부 목록에 나타나는지 확인
        List<AccountBookResponseDto> memberAccountBooks = accountBookService.getAccountBooksByUser(member1);
        assertThat(memberAccountBooks).hasSize(1);
        assertThat(memberAccountBooks.get(0).getUserRole()).isEqualTo(AccountBookRole.MEMBER);
    }

    @Test
    @DisplayName("방장만 가계부를 수정할 수 있다")
    void updateAccountBook_OnlyOwnerCanUpdate() {
        // Given - 방장이 가계부 생성하고 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        
        // When - 방장이 수정
        AccountBookUpdateRequestDto updateDto = AccountBookUpdateRequestDto.builder()
                .name("수정된 가계부")
                .description("방장이 수정한 내용")
                .build();
        AccountBookResponseDto updatedAccountBook = accountBookService.updateAccountBook(
                createdAccountBook.getId(), updateDto, owner);

        // Then
        assertThat(updatedAccountBook.getName()).isEqualTo("수정된 가계부");
        assertThat(updatedAccountBook.getDescription()).isEqualTo("방장이 수정한 내용");
    }

    @Test
    @DisplayName("멤버는 가계부를 수정할 수 없다")
    void updateAccountBook_MemberCannotUpdate() {
        // Given - 방장이 가계부 생성하고 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        
        // When & Then - 멤버가 수정 시도하면 예외 발생
        AccountBookUpdateRequestDto updateDto = AccountBookUpdateRequestDto.builder()
                .name("멤버가 수정 시도")
                .build();
        
        assertThatThrownBy(() -> accountBookService.updateAccountBook(
                createdAccountBook.getId(), updateDto, member1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가계부를 수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("방장만 가계부를 삭제할 수 있다")
    void deleteAccountBook_OnlyOwnerCanDelete() {
        // Given - 방장이 가계부 생성하고 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        Long accountBookId = createdAccountBook.getId();
        
        // When - 방장이 삭제
        accountBookService.deleteAccountBook(accountBookId, owner);
        
        // Then - 가계부가 삭제되었는지 확인
        assertThatThrownBy(() -> accountBookService.getAccountBook(accountBookId, owner))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("멤버는 가계부를 삭제할 수 없다")
    void deleteAccountBook_MemberCannotDelete() {
        // Given - 방장이 가계부 생성하고 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        
        // When & Then - 멤버가 삭제 시도하면 예외 발생
        assertThatThrownBy(() -> accountBookService.deleteAccountBook(
                createdAccountBook.getId(), member1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가계부를 삭제할 권한이 없습니다.");
    }

    @Test
    @DisplayName("방장이 멤버를 추방할 수 있다")
    void removeUserFromAccountBook_OwnerCanRemoveMember() {
        // Given - 방장이 가계부 생성하고 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        
        // When - 방장이 멤버 추방
        accountBookService.removeUserFromAccountBook(createdAccountBook.getId(), member1.getId(), owner);
        
        // Then - 추방된 멤버는 가계부에 접근할 수 없음
        assertThatThrownBy(() -> accountBookService.getAccountBook(createdAccountBook.getId(), member1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가계부에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("멤버는 다른 멤버를 추방할 수 없다")
    void removeUserFromAccountBook_MemberCannotRemoveOther() {
        // Given - 방장이 가계부 생성하고 두 멤버 추가
        AccountBookResponseDto createdAccountBook = createAccountBookWithTwoMembers();
        
        // When & Then - 멤버가 다른 멤버 추방 시도하면 예외 발생
        assertThatThrownBy(() -> accountBookService.removeUserFromAccountBook(
                createdAccountBook.getId(), member2.getId(), member1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 추방할 권한이 없습니다.");
    }

    @Test
    @DisplayName("가계부에 참여하지 않은 사용자는 접근할 수 없다")
    void getAccountBook_NonMemberCannotAccess() {
        // Given - 방장이 가계부 생성
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("비밀 가계부")
                .description("특정 사용자만 접근 가능")
                .build();
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, owner);
        
        // When & Then - 참여하지 않은 사용자가 접근 시도하면 예외 발생
        assertThatThrownBy(() -> accountBookService.getAccountBook(createdAccountBook.getId(), outsider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가계부에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("사용자는 자신이 참여한 모든 가계부 목록을 볼 수 있다")
    void getAccountBooksByUser_ReturnsAllParticipatedAccountBooks() {
        // Given - 사용자가 여러 가계부에 참여
        createAccountBookWithMember(); // 가계부 1
        createAccountBookWithMember(); // 가계부 2
        
        // When
        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(owner);
        
        // Then
        assertThat(accountBooks).hasSize(2);
        assertThat(accountBooks).allMatch(dto -> dto.getUserRole() == AccountBookRole.OWNER);
    }

    // 헬퍼 메서드들
    private AccountBookResponseDto createAccountBookWithMember() {
        // 방장이 가계부 생성
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("공유 가계부")
                .description("멤버와 함께 사용")
                .build();
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, owner);
        
        // 멤버 추가
        AccountBookJoinRequestDto joinDto = AccountBookJoinRequestDto.builder()
                .participationCode(createdAccountBook.getParticipationCode())
                .build();
        accountBookService.joinAccountBook(joinDto, member1);
        
        return createdAccountBook;
    }

    private AccountBookResponseDto createAccountBookWithTwoMembers() {
        AccountBookResponseDto createdAccountBook = createAccountBookWithMember();
        
        // 두 번째 멤버 추가
        AccountBookJoinRequestDto joinDto = AccountBookJoinRequestDto.builder()
                .participationCode(createdAccountBook.getParticipationCode())
                .build();
        accountBookService.joinAccountBook(joinDto, member2);
        
        return createdAccountBook;
    }
}
