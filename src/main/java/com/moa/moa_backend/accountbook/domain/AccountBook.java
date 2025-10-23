package com.moa.moa_backend.accountbook.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "participation_code", unique = true, nullable = false, length = 36)
    private String participationCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 가계부에 참여한 사용자들 (다대다 관계)
    @OneToMany(mappedBy = "accountBook", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountBookUser> accountBookUsers = new ArrayList<>();

    // 가계부 생성자 (방장) 추가
    public void addOwner(com.moa.moa_backend.user.domain.User user) {
        AccountBookUser accountBookUser = AccountBookUser.builder()
                .accountBook(this)
                .user(user)
                .role(AccountBookRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        this.accountBookUsers.add(accountBookUser);
    }

    // 가계부에 멤버 추가
    public void addMember(com.moa.moa_backend.user.domain.User user) {
        AccountBookUser accountBookUser = AccountBookUser.builder()
                .accountBook(this)
                .user(user)
                .role(AccountBookRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        this.accountBookUsers.add(accountBookUser);
    }

    // 사용자 제거
    public void removeUser(com.moa.moa_backend.user.domain.User user) {
        this.accountBookUsers.removeIf(accountBookUser -> 
            accountBookUser.getUser().getId().equals(user.getId()));
    }

    // 사용자가 가계부에 참여했는지 확인
    public boolean hasUser(com.moa.moa_backend.user.domain.User user) {
        return this.accountBookUsers.stream()
                .anyMatch(accountBookUser -> 
                    accountBookUser.getUser().getId().equals(user.getId()));
    }

    // 사용자의 권한 확인
    public AccountBookRole getUserRole(com.moa.moa_backend.user.domain.User user) {
        return this.accountBookUsers.stream()
                .filter(accountBookUser -> 
                    accountBookUser.getUser().getId().equals(user.getId()))
                .map(AccountBookUser::getRole)
                .findFirst()
                .orElse(null);
    }

    // 방장인지 확인
    public boolean isOwner(com.moa.moa_backend.user.domain.User user) {
        return AccountBookRole.OWNER.equals(getUserRole(user));
    }
}
