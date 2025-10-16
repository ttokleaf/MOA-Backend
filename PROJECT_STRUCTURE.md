# MOA Backend 프로젝트 구조

## 개요
요구사항 정의서를 기반으로 설계된 도메인 기반 프로젝트 구조입니다.

## 폴더 구조

```
src/main/java/com/moa/moa_backend/
├── MoaBackendApplication.java          # Spring Boot 메인 애플리케이션
│
├── user/                              # 사용자 관리 도메인
│   ├── controller/                    # 사용자 관련 API 컨트롤러
│   │   ├── AuthController.java       # 인증 관련 API
│   │   └── LoginController.java      # 로그인 관련 API
│   ├── service/                      # 사용자 비즈니스 로직
│   │   ├── AuthService.java          # 인증 서비스
│   │   └── KakaoService.java         # 카카오 로그인 서비스
│   ├── repository/                   # 사용자 데이터 접근 계층
│   │   └── UserRepository.java       # 사용자 리포지토리
│   ├── domain/                       # 사용자 엔티티
│   │   └── User.java                 # 사용자 도메인 모델
│   └── dto/                          # 사용자 관련 DTO
│       ├── KakaoTokenResponseDto.java # 카카오 토큰 응답 DTO
│       └── KakaoUserResponseDto.java  # 카카오 사용자 정보 DTO
│
├── accountbook/                      # 가계부 관리 도메인
│   ├── controller/                   # 가계부 관련 API 컨트롤러
│   ├── service/                      # 가계부 비즈니스 로직
│   ├── repository/                   # 가계부 데이터 접근 계층
│   ├── domain/                       # 가계부 엔티티
│   └── dto/                          # 가계부 관련 DTO
│
├── transaction/                      # 거래 내역 관리 도메인
│   ├── controller/                   # 거래 관련 API 컨트롤러
│   ├── service/                      # 거래 비즈니스 로직
│   ├── repository/                   # 거래 데이터 접근 계층
│   ├── domain/                       # 거래 엔티티
│   └── dto/                          # 거래 관련 DTO
│
├── recurring/                        # 반복 거래 관리 도메인
│   ├── controller/                   # 반복 거래 관련 API 컨트롤러
│   ├── service/                      # 반복 거래 비즈니스 로직
│   ├── repository/                   # 반복 거래 데이터 접근 계층
│   ├── domain/                       # 반복 거래 엔티티
│   └── dto/                          # 반복 거래 관련 DTO
│
├── budget/                           # 예산 및 재정 개요 도메인
│   ├── controller/                   # 예산 관련 API 컨트롤러
│   ├── service/                      # 예산 비즈니스 로직
│   ├── repository/                   # 예산 데이터 접근 계층
│   ├── domain/                       # 예산 엔티티
│   └── dto/                          # 예산 관련 DTO
│
├── settlement/                       # 정산 도메인
│   ├── controller/                   # 정산 관련 API 컨트롤러
│   ├── service/                      # 정산 비즈니스 로직
│   ├── repository/                   # 정산 데이터 접근 계층
│   ├── domain/                       # 정산 엔티티
│   └── dto/                          # 정산 관련 DTO
│
└── common/                           # 공통 기능
    ├── config/                       # 설정 클래스
    │   ├── CorsConfig.java           # CORS 설정
    │   ├── SecurityConfig.java       # 보안 설정
    │   └── WebClientConfig.java      # WebClient 설정
    ├── exception/                    # 예외 처리
    ├── response/                     # 공통 응답 객체
    ├── security/                     # 보안 관련 유틸리티
    └── util/                         # 공통 유틸리티
        └── SessionUtil.java          # 세션 유틸리티
```

## 도메인별 주요 기능

### 1. User (사용자 관리)
- 로그인/로그아웃
- 개인 프로필 수정
- 친구 초대하기

### 2. AccountBook (가계부 관리)
- 전체 가계부 목록 조회
- 공유 가계부 생성/참여
- 가계부 정보 조회/수정
- 가계부 사용자 목록 조회/추방
- 가계부 데이터 내보내기

### 3. Transaction (거래 내역 관리)
- 월별/일별 가계부 조회
- 수입/지출/저축 등록/수정/삭제
- 결제 수단 추가/삭제
- 특정 기간 내역 조회

### 4. Recurring (반복 거래 관리)
- 반복 일정 수정/삭제
- 반복 내역 조회

### 5. Budget (예산 및 재정 개요)
- 예산 금액 조회/수정
- 지출 분석 및 통계
- 사용자 소비 성향 조회
- 시각화 데이터 제공

### 6. Settlement (정산)
- 정산 금액 계산/수정
- 지난 정산 내역 조회

## 아키텍처 패턴

각 도메인은 다음과 같은 계층 구조를 따릅니다:

1. **Controller Layer**: REST API 엔드포인트 제공
2. **Service Layer**: 비즈니스 로직 처리
3. **Repository Layer**: 데이터 접근 계층
4. **Domain Layer**: 엔티티 및 도메인 모델
5. **DTO Layer**: 데이터 전송 객체

## 공통 기능

- **Config**: Spring 설정 클래스들
- **Exception**: 전역 예외 처리
- **Response**: 공통 API 응답 형식
- **Security**: 보안 관련 유틸리티
- **Util**: 공통 유틸리티 함수들

## AI 기능 관련

요구사항에 명시된 AI 관련 기능들은 별도 부서에서 개발되므로, 이 프로젝트 구조에는 포함되지 않습니다:
- 영수증 스캔 (OCR)
- 결제 문자 분석
- AI 리포트 및 예측
- 챗봇 기능

## 개발 가이드

1. 새로운 기능 추가 시 해당 도메인 폴더 내에서 작업
2. 공통 기능은 common 폴더 활용
3. 각 계층의 역할을 명확히 구분하여 개발
4. 도메인 간 의존성 최소화
