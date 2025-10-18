package com.moa.moa_backend.accountbook.service;

import com.moa.moa_backend.accountbook.domain.AccountBookRole;
import com.moa.moa_backend.accountbook.dto.AccountBookCreateRequestDto;
import com.moa.moa_backend.accountbook.dto.AccountBookResponseDto;
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
@DisplayName("가계부 간단 통합 테스트")
class AccountBookSimpleIntegrationTest {

    @Autowired
    private AccountBookService accountBookService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .kakaoId(12345L)
                .nickname("통합테스트유저")
                .email("integration@example.com")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("가계부 생성 및 조회 테스트")
    void createAndGetAccountBook() {
        // Given
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("통합 테스트 가계부")
                .description("간단 통합 테스트용 가계부")
                .build();

        // When
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, testUser);

        // Then
        assertThat(createdAccountBook).isNotNull();
        assertThat(createdAccountBook.getName()).isEqualTo("통합 테스트 가계부");
        assertThat(createdAccountBook.getDescription()).isEqualTo("간단 통합 테스트용 가계부");
        assertThat(createdAccountBook.getParticipationCode()).isNotNull();
        assertThat(createdAccountBook.getUserRole()).isEqualTo(AccountBookRole.OWNER);

        // 생성된 가계부 조회
        AccountBookResponseDto retrievedAccountBook = accountBookService.getAccountBook(createdAccountBook.getId(), testUser);
        assertThat(retrievedAccountBook).isNotNull();
        assertThat(retrievedAccountBook.getId()).isEqualTo(createdAccountBook.getId());
        assertThat(retrievedAccountBook.getName()).isEqualTo("통합 테스트 가계부");
    }

    @Test
    @DisplayName("사용자의 가계부 목록 조회 테스트")
    void getAccountBooksByUser() {
        // Given - 가계부 생성
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("목록 조회 테스트 가계부")
                .description("목록 조회용 가계부")
                .build();
        accountBookService.createAccountBook(createDto, testUser);

        // When
        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(testUser);

        // Then
        assertThat(accountBooks).hasSize(1);
        assertThat(accountBooks.get(0).getName()).isEqualTo("목록 조회 테스트 가계부");
        assertThat(accountBooks.get(0).getUserRole()).isEqualTo(AccountBookRole.OWNER);
    }

    @Test
    @DisplayName("여러 가계부 생성 및 관리 테스트")
    void createMultipleAccountBooks() {
        // Given & When - 여러 가계부 생성
        AccountBookCreateRequestDto createDto1 = AccountBookCreateRequestDto.builder()
                .name("가계부 1")
                .description("첫 번째 가계부")
                .build();
        AccountBookCreateRequestDto createDto2 = AccountBookCreateRequestDto.builder()
                .name("가계부 2")
                .description("두 번째 가계부")
                .build();

        AccountBookResponseDto accountBook1 = accountBookService.createAccountBook(createDto1, testUser);
        AccountBookResponseDto accountBook2 = accountBookService.createAccountBook(createDto2, testUser);

        // Then
        assertThat(accountBook1).isNotNull();
        assertThat(accountBook2).isNotNull();
        assertThat(accountBook1.getName()).isEqualTo("가계부 1");
        assertThat(accountBook2.getName()).isEqualTo("가계부 2");

        // 사용자의 가계부 목록 확인
        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(testUser);
        assertThat(accountBooks).hasSize(2);
        assertThat(accountBooks).extracting(AccountBookResponseDto::getName)
                .containsExactlyInAnyOrder("가계부 1", "가계부 2");
    }

    @Test
    @DisplayName("가계부 기본 정보 검증 테스트")
    void validateAccountBookBasicInfo() {
        // Given
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("기본 정보 테스트 가계부")
                .description("기본 정보 검증용")
                .profileImageUrl("https://example.com/image.jpg")
                .build();

        // When
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, testUser);

        // Then - 기본 정보 검증
        assertThat(createdAccountBook.getId()).isNotNull();
        assertThat(createdAccountBook.getName()).isEqualTo("기본 정보 테스트 가계부");
        assertThat(createdAccountBook.getDescription()).isEqualTo("기본 정보 검증용");
        assertThat(createdAccountBook.getProfileImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(createdAccountBook.getParticipationCode()).isNotNull();
        assertThat(createdAccountBook.getParticipationCode()).isNotEmpty();
        assertThat(createdAccountBook.getUserRole()).isEqualTo(AccountBookRole.OWNER);
        assertThat(createdAccountBook.getCreatedAt()).isNotNull();
        assertThat(createdAccountBook.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("가계부 생성 시 방장 권한 확인 테스트")
    void verifyOwnerRoleOnCreation() {
        // Given
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("권한 확인 가계부")
                .description("방장 권한 확인용")
                .build();

        // When
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, testUser);

        // Then
        assertThat(createdAccountBook.getUserRole()).isEqualTo(AccountBookRole.OWNER);
        
        // 조회 시에도 권한 확인
        AccountBookResponseDto retrievedAccountBook = accountBookService.getAccountBook(createdAccountBook.getId(), testUser);
        assertThat(retrievedAccountBook.getUserRole()).isEqualTo(AccountBookRole.OWNER);
    }

    @Test
    @DisplayName("가계부 참여 코드 생성 테스트")
    void verifyParticipationCodeGeneration() {
        // Given
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("참여 코드 테스트 가계부")
                .description("참여 코드 생성 확인용")
                .build();

        // When
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, testUser);

        // Then
        assertThat(createdAccountBook.getParticipationCode()).isNotNull();
        assertThat(createdAccountBook.getParticipationCode()).isNotEmpty();
        
        // 참여 코드가 UUID 형식인지 확인 (실제 구현에 따라 조정)
        String participationCode = createdAccountBook.getParticipationCode();
        assertThat(participationCode.length()).isGreaterThan(10); // 최소 길이 확인
    }

    @Test
    @DisplayName("가계부 생성 및 삭제 생명주기 테스트")
    void accountBookLifecycleTest() {
        // Given
        AccountBookCreateRequestDto createDto = AccountBookCreateRequestDto.builder()
                .name("생명주기 테스트 가계부")
                .description("생명주기 확인용")
                .build();

        // When - 생성
        AccountBookResponseDto createdAccountBook = accountBookService.createAccountBook(createDto, testUser);
        Long accountBookId = createdAccountBook.getId();

        // Then - 생성 확인
        assertThat(createdAccountBook).isNotNull();
        
        // 목록에 존재하는지 확인
        List<AccountBookResponseDto> accountBooks = accountBookService.getAccountBooksByUser(testUser);
        assertThat(accountBooks).hasSize(1);
        assertThat(accountBooks.get(0).getId()).isEqualTo(accountBookId);

        // When - 삭제
        accountBookService.deleteAccountBook(accountBookId, testUser);

        // Then - 삭제 확인
        assertThatThrownBy(() -> accountBookService.getAccountBook(accountBookId, testUser))
                .isInstanceOf(Exception.class); // 삭제된 가계부 접근 시 예외

        // 목록에서 제거되었는지 확인
        List<AccountBookResponseDto> accountBooksAfterDelete = accountBookService.getAccountBooksByUser(testUser);
        assertThat(accountBooksAfterDelete).isEmpty();
    }
}
