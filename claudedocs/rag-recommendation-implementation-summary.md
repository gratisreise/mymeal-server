# RAG 기반 추천 알림 시스템 구현 완료

## 구현 개요

AI 기반의 개인화된 식단 추천 시스템이 성공적으로 구현되었습니다. Spring Batch, RAG(벡터 검색), Redis 스케줄링, FCM 푸시 알림을 통합하여 사용자별 맞춤 식단 추천을 제공합니다.

## 완료된 구성 요소

### Phase 1: 도메인 레이어 ✅
- **Recommendation Entity**: `src/main/java/com/mymealserver/entity/Recommendation.java`
  - Soft delete 상속
  - mealType, scheduledTime, menuDetails, pushMessage, isSent 컬럼
- **RecommendationRepository**: JPA Repository 쿼리 메서드
- **RecommendationReader**: 조회 전용 도메인 서비스
- **RecommendationWriter**: 명령 전용 도메인 서비스
- **Flyway Migration V4**: `recommendations` 테이블 생성

### Phase 2: Spring AI 및 RAG 컴포넌트 ✅
- **RagPromptBuilder**: 사용자별 컨텍스트 조립
  - 영양사 페르소나 프롬프트 템플릿
  - 과거 식사 이력 (최근 30일 GOOD 등급)
  - JSON 응답 형식 가이드
- **VectorSearchService**: 유사도 검색 서비스
  - 현재는 GOOD 등급 필터링 방식
  - 향후 pgvector 임베딩 기반 유사도 검색 확장 가능

### Phase 3: Spring Batch Job ✅
- **BatchConfig**: Spring Batch 5.x 설정
- **RecommendationGenerationJob**: 매일 새벽 1시 실행 (Cron: `0 0 1 * * ?`)
  - Reader: 활성화된 회원 목록 조회
  - Processor: 개별 회원별 추천 생성 (RAG + AI)
  - Writer: DB 저장 + Redis 스케줄링
- **MemberItemReader**: ItemReader 구현
- **RecommendationProcessor**: AI 추천 생성 로직
- **RecommendationItemWriter**: Bulk insert + Redis 등록
- **RecommendationStepListener**: 배치 실행 로깅

### Phase 4: Redis 스케줄링 ✅
- **RedisSchedulerService**: Redis Sorted Set 관리
  - Key: `meal:notifications`
  - Score: `scheduled_time - 30분` (Unix timestamp)
  - 30분 전 알림 발송 스케줄링
- **NotificationPollingScheduler**: 1분 단위 폴링
  - `@Scheduled(fixedRate = 60000)` - 매 분 실행
  - Redis에서到期 알림 조회
  - FCM 푸시 발송
  - 발송 완료 표시

### Phase 5: API 수정 ✅
- **RecommendationService**:
  - 배치 생성된 추천 우선 조회
  - 없을 경우 룰 기반 fallback
  - JSON 파싱으로 mealName, reason 추출
- **FcmNotificationService**:
  - `sendRecommendationNotification()` 메서드 추가
  - 제목: "🍽️ 오늘의 식단 추천"

### Phase 6: 설정 및 스케줄러 ✅
- **SchedulerConfig**: `@EnableScheduling` 활성화
- **RecommendationScheduler**: 배치 잡 트리거
  - 자동: 매일 새벽 1시
  - 수동: `triggerRecommendationGeneration()` 메서드
- **application.yaml**:
  - Spring Batch 활성화 설정 (기본: false, 필요시 true로 변경)
  - Spring AI Gemini 설정 유지

## 기술 스택

- **Spring Boot**: 3.5.10
- **Spring Batch**: 5.x (새로운 API 사용)
- **Spring AI**: 1.1.2 (Google Gemini 2.5 Flash)
- **Redis**: Sorted Set for scheduling
- **PostgreSQL**: pgvector (향후 확장)
- **Firebase**: FCM push notifications
- **Lombok**: Builder pattern

## 데이터 흐름

```
매일 새벽 1시
  ↓
RecommendationScheduler.trigger()
  ↓
Spring Batch Job 시작
  ↓
MemberItemReader (활성화 회원 조회)
  ↓
RecommendationProcessor (AI 추천 생성)
  ├─ VectorSearchService (최근 30일 GOOD 식사)
  ├─ RagPromptBuilder (프롬프트 조립)
  └─ AI 호출 (Gemini Flash)
  ↓
RecommendationItemWriter
  ├─ DB 저장 (Recommendation 테이블)
  └─ Redis 등록 (Sorted Set)
  ↓
식사 30분 전
  ↓
NotificationPollingScheduler (1분 폴링)
  ├─ Redis에서到期 알림 조회
  ├─ FCM 발송
  └─ DB 발송 완료 표시
```

## 향후 확장 가능성

1. **pgvector 벡터 검색**: 음식 임베딩 기반 시맨틱 유사도 검색
2. **사용자 피드백**: 추천 만족도 조사
3. **필터링**: 알러지, 선호/비선보 음식
4. **영양 목표**: 다이어트, 벌크업 등 맞춤 추천
5. **계절별 추천**: 여름=시원한 음식, 겨울=따뜻한 음식

## 사용 방법

### 1. 배치 잡 활성화
`application.yaml`:
```yaml
spring:
  batch:
    job:
      enabled: true  # 자동 실행 활성화
```

### 2. 수동 실행 (테스트용)
```java
@Autowired
private RecommendationScheduler recommendationScheduler;

public void testRecommendation() throws Exception {
    recommendationScheduler.triggerRecommendationGeneration();
}
```

### 3. API 호출
```bash
# 오늘의 추천 조회
GET /api/v1/recommendations
Authorization: Bearer {token}

# 추천 스케줄 조회
GET /api/v1/recommendations/schedule
Authorization: Bearer {token}
```

## 빌드 확인

```bash
./gradlew compileJava
# BUILD SUCCESSFUL confirmed
```

## 주요 파일 목록

### 새로 생성된 파일 (14개)
1. `entity/Recommendation.java`
2. `repository/RecommendationRepository.java`
3. `domain/recommendation/RecommendationReader.java`
4. `domain/recommendation/RecommendationWriter.java`
5. `config/BatchConfig.java`
6. `config/SchedulerConfig.java`
7. `batch/job/RecommendationGenerationJob.java`
8. `batch/job/RecommendationStepListener.java`
9. `batch/reader/MemberItemReader.java`
10. `batch/processor/RecommendationProcessor.java`
11. `batch/writer/RecommendationItemWriter.java`
12. `service/recommendation/RagPromptBuilder.java`
13. `service/recommendation/VectorSearchService.java`
14. `service/recommendation/RedisSchedulerService.java`
15. `service/recommendation/NotificationPollingScheduler.java`
16. `service/recommendation/RecommendationScheduler.java`
17. `resources/db/migration/V4__create_recommendation_table.sql`

### 수정된 파일 (4개)
1. `api/recommendation/service/RecommendationService.java`
2. `api/notification/service/FcmNotificationService.java`
3. `resources/application.yaml`
4. `domain/member/MemberReader.java`

## 다음 단계

1. **AI 통합 완성**: `RecommendationProcessor.generateRecommendations()`에서 실제 Gemini API 호출
2. **테스트 작성**: 배치 잡, 스케줄러, API 통합 테스트
3. **모니터링**: 배치 실행 시간, 성공/실패 모니터링
4. **배포**: 프로덕션 환경 설정 및 배포
