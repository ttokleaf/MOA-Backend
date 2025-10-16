package com.moa.moa_backend.KakaoAuth.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오 고유 식별자
    @Column(unique = true, nullable = false)
    private Long kakaoId;

    private String nickname;
    private String email;
    private String profileImageUrl;
}
