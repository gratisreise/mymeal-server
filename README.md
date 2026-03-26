# mymeal-server

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> 개인 식사 추적 및 건강 분석 서비스 백엔드 API

mymeal-server는 사용자가 식사 사진을 기록하고, 식후 신체 반응을 체크하며, AI 기반 음식 분석과 맞춤형 추천을 받을 수 있는 개인 건강 관리 서비스입니다.

## 주요 기능

- 🔐 **소셜 로그인**: Google, Naver, Kakao OAuth2 인증
- 📸 **식사 기록**: 사진 첨부 + AI 기반 음식 분석
- 😐 **반응 기록**: 식후 소화, 포만감, 에너지 레벨 체크
- 📅 **캘린더 뷰**: 일간/월간 식사 및 반응 패턴 확인
- 🏆 **랭킹**: 최고/최악 음식 및 일자 순위
- 📊 **통계**: 개인 식사 통계 및 신체 패턴 분석
- 🔔 **알림**: 식사 리마인더, 반응 기록 알림, 추천 알림 (FCM)
- 💡 **AI 추천**: 사용자 패턴 기반 맞춤형 식사 추천
- 🎯 **프로필**: 사용자 정보 및 설정 관리

## 기술 스택

### 백엔드
- **Java**: 21 
- **Spring Boot**: 3.5.10
- **Spring Data JPA**: ORM
- **Spring Security**: 인증/인가
- **Spring Batch**: 배치 처리
- **Spring AI**: 1.1.0 (AI 통합)
- **Spotless**: 정적분석

### 데이터베이스 & 캐싱
- **PostgreSQL**: 16+ (pgvector 확장 포함)
- **Redis**: 캐싱 및 세션 관리

### 외부 서비스
- **AWS S3**: 이미지 스토리지
- **Firebase FCM**: 푸시 알림
- **Google GenAI**: 이미지 분석 및 임베딩
- **OAuth2**: Google, Naver, Kakao

### 보안
- **JWT**: 토큰 기반 인증 (JJWT 0.13.0)
- **Spring Security**: 보안 필터 체인

### 모니터링
- **Spring Boot Actuator**: 애플리케이션 헬스체크
- **Micrometer Prometheus**: 메트릭 수집
- **Grafana**: 대시보드 시각화

### 테스트
- **JUnit 5**: 단위 테스트
- **Mockito**: 격리

## 아키텍처
![시스템아키텍처](https://diagrams-noaahh.s3.ap-northeast-2.amazonaws.com/mymeal_system_architec.png)

### 주요 패턴

- **Reader/Writer Pattern**: 읽기 쓰기 분리
- **Soft Delete**: 데이터 영구 보존 (`deleted_at` 컬럼)
- **DTO Pattern**: 요청/응답 분리

## API 문서

공개적인 api 문서는 apidog으로 공유합니다.:

- **Apidog**: https://h2wztsadhp.apidog.io

## API 엔드포인트

### 인증 (`/api/v1/auth`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/register` | 이메일 회원가입 |
| POST | `/login` | 이메일 로그인 |
| POST | `/oauth` | 소셜 로그인 |
| POST | `/refresh` | 토큰 갱신 |
| POST | `/logout` | 로그아웃 |
| DELETE | `/withdraw` | 회원 탈퇴 |

### 식사 (`/api/v1/meals`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/meals` | 식사 생성 (사진 업로드 + AI 분석) |
| GET | `/meals` | 식사 목록 (페이지네이션/필터링) |
| GET | `/meals/{id}` | 식사 상세 조회 |
| DELETE | `/meals/{id}` | 식사 삭제 |
| POST | `/meals/{id}/photo` | 사진 재촬영 |

### 반응 (`/api/v1/meals/{id}/reactions`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/reactions` | 식후 반응 기록 |
| GET | `/reactions` | 특정 식사의 반응 조회 |
| GET | `/reactions/statistics` | 반응 통계 |

### 캘린더 (`/api/v1/calendar`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/calendar/daily` | 일간 캘린더 데이터 |
| GET | `/calendar/monthly` | 월간 캘린더 데이터 |
| GET | `/calendar/monthly-summary` | 월간 요약 |

### 랭킹 (`/api/v1/ranking`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/ranking/best-foods` | 최고 음식 랭킹 |
| GET | `/ranking/worst-foods` | 최악 음식 랭킹 |
| GET | `/ranking/best-days` | 최고 식사 일자 |
| GET | `/ranking/worst-days` | 최악 식사 일자 |

### 추천 (`/api/v1/recommendations`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/recommendations` | 맞춤형 식사 추천 |
| GET | `/recommendations/schedule` | 추천 스케줄 |
| POST | `/recommendations/feedback` | 추천 피드백 |

### 프로필 (`/api/v1/profile`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/profile` | 사용자 프로필 조회 |
| PUT | `/profile` | 프로필 수정 |
| GET | `/statistics` | 사용자 통계 |
| GET | `/patterns` | 신체 패턴 분석 |

### 설정 (`/api/v1/settings`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/settings` | 사용자 설정 조회 |
| PUT | `/settings` | 설정 수정 |

### 알림 (`/api/v1/notifications`)
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/notifications` | 알림 목록 |
| PUT | `/notifications/settings` | 알림 설정 |

**총 45개 API 엔드포인트 제공**

## 데이터베이스 스키마
### ERD
![ERD](https://diagrams-noaahh.s3.ap-northeast-2.amazonaws.com/mymeal_erd.png)

### 주요 테이블

| 테이블명 | 설명 |
|---------|------|
| `members` | 사용자 계정 |
| `member_settings` | 사용자 설정 |
| `member_withdrawals` | 회원 탈퇴 기록 |
| `meals` | 식사 기록 |
| `meal_analyses` | AI 분석 결과 |
| `foods` | 음식 마스터 데이터 |
| `food_member_stats` | 사용자별 음식 통계 |
| `reactions` | 식후 반응 |
| `notifications` | 푸시 알림 기록 |


## 프로젝트 구조

```
src/main/java/com/mymealserver/
├── MymealServerApplication.java        # 메인 진입점
├── common/                              # 공통 컴포넌트
│   ├── response/                       # API 응답 구조
│   ├── exception/                      # 예외 처리
│   ├── filter/                         # 보안 필터
│   └── resolver/                       # 인자 리졸버
├── config/                              # Spring 설정
├── entity/                              # JPA 엔티티
├── repository/                          # Data JPA 리포지토리
├── domain/                              # 도메인 계층 (Reader/Writer)
├── service/                             # 비즈니스 로직
├── external/                            # 외부 통합
│   ├── batch/                          # 배치 처리
│   └── llm/                            # LLM 서비스
└── controller/                          # REST API 컨트롤러
```
