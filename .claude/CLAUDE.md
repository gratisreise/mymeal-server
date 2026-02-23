# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**mymeal-server** is a personal meal tracking and analysis service built with Spring Boot 3.5.10 and Java 21. The application helps users:
- Track meals with photos
- Record post-meal body reactions
- Receive AI-powered food analysis and meal recommendations
- Monitor eating patterns and health trends

**Current Status**: Production-ready implementation with 45 API endpoints across 9 controllers, comprehensive business logic, 17 test classes, and complete Reader/Writer domain pattern implementation.

---

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application (local profile)
./gradlew bootRun --args='--spring.profiles.active=local'

# Run all tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Build and run tests together
./gradlew clean build

# Create OCI image (Docker)
./gradlew bootBuildImage

# Run with specific profile
./gradlew bootRun -Dspring.profiles.active=prod
```

---

## Project Structure

### Architecture Overview

**mymeal-server** follows **DDD (Domain-Driven Design)** + **Clean Architecture** principles with Reader/Writer pattern for domain logic.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                       │
│              (Controller + DTO + Validation)                 │
├─────────────────────────────────────────────────────────────────┤
│                       Application Layer                      │
│                   (Service + Domain Reader/Writer)            │
├─────────────────────────────────────────────────────────────────┤
│                        Domain Layer                          │
│              (Entity + Repository Interface)                  │
├─────────────────────────────────────────────────────────────────┤
│                     Infrastructure Layer                     │
│       (Repository Impl + External API + Config)              │
└─────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
src/main/java/com/mymealserver/
├── MymealServerApplication.java              # Main entry point
│
├── common/                                    # Cross-cutting concerns
│   ├── response/                             # API response structure
│   │   ├── BaseResponse.java                # Abstract base for all responses
│   │   ├── SuccessResponse.java             # Success response wrapper
│   │   ├── ErrorResponse.java               # Error response wrapper
│   │   ├── PageResponse.java                # Paginated response
│   │   └── classes/
│   │       ├── Pagination.java              # Pagination metadata
│   │       └── ErrorDetail.java             # Error detail structure
│   │
│   ├── exception/                            # Exception handling
│   │   ├── GlobalExceptionHandler.java     # @ControllerAdvice
│   │   ├── BusinessException.java           # Custom business exception
│   │   └── ErrorCode.java                    # Error code enumeration
│   │
│   ├── filter/                               # Security filters
│   │   └── JwtAuthenticationFilter.java     # JWT authentication filter
│   │
│   └── resolver/                             # Custom argument resolvers
│       └── AuthenticatedMemberArgumentResolver.java  # @AuthenticatedMember injection
│
├── config/                                    # Spring configuration
│   ├── SecurityConfig.java                   # Spring Security + JWT
│   ├── JwtTokenProvider.java                # JWT token generation/validation
│   ├── RedisConfig.java                     # Redis configuration
│   ├── AwsS3Config.java                     # AWS S3 client
│   ├── FirebaseConfig.java                  # Firebase Admin SDK
│   └── OpenApiConfig.java                   # SpringDoc OpenAPI/Swagger
│
├── entity/                                    # JPA Entities (Domain Models)
│   ├── BaseEntity.java                      # Base entity with createdAt/updatedAt
│   ├── SoftDeletable.java                   # Soft delete support
│   ├── Member.java                          # User/member entity
│   ├── MemberSettings.java                  # Member settings
│   ├── MemberWithdrawal.java                # Withdrawal history
│   ├── Meal.java                            # Meal record
│   ├── MealAnalysis.java                    # AI food analysis result
│   ├── Food.java                            # Food master data
│   ├── FoodMemberStats.java                 # Per-member food stats
│   ├── Reaction.java                        # Post-meal reaction
│   └── Notification.java                    # Push notification
│
├── entity/enums/                             # JPA Enums
│   ├── ProviderType.java                    # EMAIL, GOOGLE, NAVER, KAKAO
│   ├── MealType.java                        # BREAKFAST, LUNCH, DINNER, SNACK
│   ├── AnalysisStatus.java                  # PENDING, PROCESSING, COMPLETED, FAILED
│   ├── GradeType.java                       # GOOD, NORMAL, BAD
│   ├── NotificationType.java                # RECOMMENDATION, REACTION_REMINDER, MEAL_REMINDER
│   └── WithdrawalReason.java                # Withdrawal reason codes
│
├── repository/                               # Spring Data JPA Repositories
│   ├── MemberRepository.java
│   ├── MemberSettingsRepository.java
│   ├── MemberWithdrawalRepository.java
│   ├── MealRepository.java
│   ├── MealAnalysisRepository.java
│   ├── FoodRepository.java
│   ├── FoodMemberStatsRepository.java
│   ├── ReactionRepository.java
│   └── NotificationRepository.java
│
├── domain/                                   # Domain Layer (Reader/Writer Pattern)
│   ├── member/
│   │   ├── MemberReader.java                # Member query operations
│   │   └── MemberWriter.java                # Member command operations
│   ├── memberSettings/
│   │   ├── MemberSettingsReader.java        # MemberSettings query operations
│   │   └── MemberSettingsWriter.java        # MemberSettings command operations
│   ├── meal/
│   │   ├── MealReader.java                  # Meal query operations
│   │   ├── MealWriter.java                  # Meal command operations
│   │   ├── MealAnalysisReader.java          # Analysis query operations
│   │   └── MealAnalysisWriter.java          # Analysis command operations
│   ├── food/
│   │   ├── FoodReader.java                  # Food query operations
│   │   └── FoodWriter.java                  # Food command operations
│   ├── reaction/
│   │   ├── ReactionReader.java              # Reaction query operations
│   │   └── ReactionWriter.java              # Reaction command operations
│   ├── notification/
│   │   ├── NotificationReader.java          # Notification query operations
│   │   └── NotificationWriter.java          # Notification command operations
│   └── calendar/
│       ├── CalendarReader.java              # Calendar query operations
│       ├── CalendarWriter.java              # Calendar command operations
│       └── CalendarDataAggregator.java      # Calendar data aggregation
│
├── service/                                  # Application Layer (Business Logic)
│   ├── auth/
│   │   ├── AuthService.java                 # Authentication business logic
│   │   ├── OAuthService.java                # OAuth2 interface
│   │   ├── OAuthServiceFactory.java         # OAuth provider factory
│   │   ├── TokenService.java                # JWT token management
│   │   ├── TokenBlacklistService.java       # Token blacklist for logout
│   │   └── impl/                            # OAuth provider implementations
│   │       ├── GoogleOAuthService.java      # Google OAuth2
│   │       ├── NaverOAuthService.java       # Naver OAuth2
│   │       └── KakaoOAuthService.java       # Kakao OAuth2
│   ├── client/                              # External API clients
│   │   ├── google/
│   │   │   ├── GoogleApiClient.java         # Google API client
│   │   │   ├── GoogleTokenResponse.java     # Token response DTO
│   │   │   └── GoogleUserInfoResponse.java  # User info response DTO
│   │   ├── naver/
│   │   │   ├── NaverApiClient.java          # Naver API client
│   │   │   ├── NaverTokenResponse.java      # Token response DTO
│   │   │   └── NaverUserInfoResponse.java   # User info response DTO
│   │   └── kakao/
│   │       ├── KakaoApiClient.java          # Kakao API client
│   │       ├── KakaoTokenResponse.java      # Token response DTO
│   │       └── KakaoUserInfoResponse.java   # User info response DTO
│   ├── meal/
│   │   ├── MealService.java                 # Meal CRUD operations
│   │   └── MealAnalysisService.java         # AI food analysis orchestration
│   ├── reaction/
│   │   └── ReactionService.java             # Post-meal reaction logic
│   ├── calendar/
│   │   └── CalendarService.java             # Calendar data aggregation
│   ├── ranking/
│   │   └── RankingService.java              # Best/worst meal ranking
│   ├── recommendation/
│   │   ├── RecommendationService.java       # Meal recommendation logic
│   │   ├── AiAnalysisService.java           # AI integration service
│   │   └── RecommendationScheduler.java     # Scheduled recommendation jobs
│   ├── profile/
│   │   ├── ProfileService.java              # User profile management
│   │   ├── StatisticsService.java           # User statistics
│   │   └── BodyPatternService.java          # Body pattern analysis
│   ├── settings/
│   │   └── SettingsService.java             # User settings management
│   ├── notification/
│   │   ├── NotificationService.java         # Notification management
│   │   └── FcmNotificationService.java      # FCM push notification
│   └── storage/
│       └── FileStorageService.java          # S3 file upload/download
│
├── controller/                               # Presentation Layer (REST API)
│   ├── AuthController.java                  # /api/v1/auth/** (6 endpoints)
│   ├── MealController.java                  # /api/v1/meals/** (5 endpoints)
│   ├── ReactionController.java              # /api/v1/meals/{id}/reactions (3 endpoints)
│   ├── CalendarController.java              # /api/v1/calendar/** (3 endpoints)
│   ├── RankingController.java               # /api/v1/ranking/** (4 endpoints)
│   ├── RecommendationController.java       # /api/v1/recommendations/** (3 endpoints)
│   ├── ProfileController.java               # /api/v1/profile/** (4 endpoints)
│   ├── SettingsController.java              # /api/v1/settings/** (2 endpoints)
│   └── NotificationController.java          # /api/v1/notifications/** (2 endpoints)
│
└── api/                                      # API-specific packages (DTOs)
    ├── auth/                                 # Auth DTOs
    ├── meal/                                 # Meal DTOs
    ├── reaction/                             # Reaction DTOs
    ├── calendar/                             # Calendar DTOs
    ├── ranking/                              # Ranking DTOs
    ├── recommendation/                       # Recommendation DTOs
    ├── profile/                              # Profile DTOs
    ├── settings/                             # Settings DTOs
    └── notification/                         # Notification DTOs
```

### Resource Files

```
src/main/resources/
├── application.yaml                         # Base configuration (shared)
├── application-local.yaml                   # Local development profile
├── application-prod.yaml                    # Production profile
├── messages.properties                      # Validation message properties (i18n)
└── db/migration/                            # Flyway database migrations
    ├── V1__Init.sql                         # Initial schema
    ├── V2__*.sql                            # Subsequent migrations
    └── ...
```

---

## Technology Stack

### Core Technologies
- **Java**: 21 (Virtual Threads enabled)
- **Spring Boot**: 3.5.10
- **Build Tool**: Gradle 8.x
- **Database**: PostgreSQL 15+ (with pgvector extension)
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway

### Key Dependencies

**Security & Authentication**:
- Spring Security 6.x
- JJWT 0.13.0 (JWT token generation/validation)
- Spring Security OAuth2 Client (Google, Naver, Kakao)

**Data & Caching**:
- Spring Data JPA
- Spring Data Redis
- HikariCP (connection pool)

**Storage**:
- AWS SDK for Java v2 (2.31.0) - S3 integration
- Firebase Admin SDK (9.7.0) - FCM push notifications

**AI/ML**:
- pgvector 0.1.4 (PostgreSQL vector similarity search)
- Spring AI 1.0.0-M4 (commented out, prepared for future use)

**Batch & Scheduling**:
- Spring Batch
- Spring Scheduler (Virtual Thread Task Executor)

**API Documentation**:
- SpringDoc OpenAPI 2.8.9 (Swagger UI)

**Monitoring**:
- Spring Boot Actuator
- Micrometer Prometheus

**Testing**:
- JUnit 5
- Spring Boot Test
- Testcontainers (PostgreSQL)

**Utilities**:
- Lombok (code generation)
- Spring Validation (bean validation)
- Spring MessageSource (validation message centralization)

---

## Architecture Patterns

### Reader/Writer Pattern (Domain Layer)

The domain layer follows the **Reader/Writer pattern** to separate query (read) and command (write) operations:

**Reader**: Handles query operations (SELECT, read-only)
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReader {
    private final MemberRepository memberRepository;

    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
```

**Writer**: Handles command operations (INSERT, UPDATE, DELETE)
```java
@Service
@RequiredArgsConstructor
public class MemberWriter {
    private final MemberRepository memberRepository;

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }
}
```

**Benefits**:
- Clear separation of concerns (CQRS-like)
- Easier to reason about side effects
- Better testability
- Explicit transaction boundaries

**Implemented Domain Readers/Writers**:
- `MemberReader/Writer` - Member entity operations
- `MemberSettingsReader/Writer` - Member settings operations
- `MealReader/Writer` - Meal entity operations
- `MealAnalysisReader/Writer` - AI analysis operations
- `FoodReader/Writer` - Food master data operations
- `ReactionReader/Writer` - Post-meal reaction operations
- `NotificationReader/Writer` - Notification operations
- `CalendarReader/Writer` - Calendar data operations

### Layered Architecture

**Controller Layer** (Presentation):
- Handles HTTP request/response
- Request validation with `@Valid`
- Returns standardized `BaseResponse<T>` format
- No business logic

**Service Layer** (Application):
- Business logic orchestration
- Transaction management with `@Transactional`
- Integrates domain Readers/Writers
- External API calls (AI, OAuth, FCM, S3)

**Domain Layer**:
- Entity models with encapsulated business rules
- Reader/Writer interfaces for data access
- Repository interfaces (Spring Data JPA)

**Infrastructure Layer**:
- Repository implementations (Spring Data JPA proxies)
- External service clients (AWS, Firebase, OAuth providers)
- Configuration beans

### Soft Delete Pattern

All major entities extend `SoftDeletable`:
```java
@MappedSuperclass
public class SoftDeletable {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

Queries must filter: `WHERE deleted_at IS NULL`

### Authentication Pattern

**@AuthenticatedMember Annotation**:
- Custom annotation for injecting authenticated member
- Used with `AuthenticatedMemberArgumentResolver`
- Eliminates boilerplate code for accessing current user

```java
@GetMapping("/profile")
public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile(
    @AuthenticatedMember Member member  // Auto-injected from JWT
) {
    return ResponseEntity.ok(
        new SuccessResponse<>(profileService.getProfile(member))
    );
}
```

---

## Development Guidelines

### Adding New Features

1. **Define Entity**: Create in `entity/` package
2. **Create Repository**: Interface in `repository/` package
3. **Create Domain Layer**: Add `{Entity}Reader` and `{Entity}Writer` in `domain/{entity}/`
4. **Implement Service**: Add business logic in `service/{feature}/`
5. **Create Controller**: Add REST endpoints in `controller/`
6. **Define DTOs**: Create request/response DTOs in `api/{feature}/dto/`

### Code Style

**Entities**:
- Use Lombok `@Builder` for object creation
- Private no-args constructor for JPA
- Encapsulate business logic in entity methods
- Extend `SoftDeletable` for soft delete support

```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal extends SoftDeletable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business logic methods
    public void completeAnalysis() {
        this.analysisStatus = AnalysisStatus.COMPLETED;
    }
}
```

**Controllers**:
- Use `@RestController` and `@RequestMapping("/api/v1/{domain}")`
- Inject service with `final` field + `@RequiredArgsConstructor`
- Return `SuccessResponse<T>` or `ErrorResponse`
- Validate requests with `@Valid`
- Add Swagger annotations (`@Tag`, `@Operation`)

```java
@Slf4j
@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
@Tag(name = "Meal", description = "식사 관리")
public class MealController {

    private final MealService mealService;

    @PostMapping
    @Operation(summary = "식사 생성", description = "새로운 식사 기록을 생성합니다")
    public ResponseEntity<SuccessResponse<MealResponse>> createMeal(
        @Valid @ModelAttribute MealCreateRequest request
    ) {
        MealResponse response = mealService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new SuccessResponse<>(response));
    }
}
```

**Services**:
- Use `@Service` and `@RequiredArgsConstructor`
- Default `@Transactional(readOnly = true)` on class
- Explicit `@Transactional` for write operations
- Inject domain Readers/Writers, not Repositories

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {

    private final MealReader mealReader;
    private final MealWriter mealWriter;
    private final MealAnalysisService mealAnalysisService;

    @Transactional
    public MealResponse createMeal(MealCreateRequest request) {
        // Business logic
    }
}
```

**DTOs**:
- Use `record` for immutable request/response objects
- Always use validation annotations from `jakarta.validation.constraints`
- **CRITICAL**: All validation messages MUST use message keys from `messages.properties`
- Use `{key}` format for message attributes (Spring will auto-resolve from MessageSource)
- Follow naming convention: `validation.{domain}.{field}.{constraint}`

```java
@Builder
public record MealCreateRequest(

        @NotBlank(message = "{validation.meal.photo.notblank}")
        MultipartFile photo,

        @NotNull(message = "{validation.meal.mealType.notnull}")
        MealType mealType,

        @Size(max = 200, message = "{validation.common.size.max}")
        String memo
) {
}
```

**Validation Message Convention**:
1. **DO NOT hardcode messages** in DTOs - always use message keys
2. **Add new keys** to `src/main/resources/messages.properties` when creating new validations
3. **Use descriptive key hierarchy**: `validation.{domain}.{field}.{constraint}`
4. **Common patterns**:
   - `{validation.email.notblank}` - Email required field
   - `{validation.password.size.min}` - Password minimum length
   - `{validation.common.size.max}` - Generic max size (with fieldName, max placeholders)
   - `{validation.common.range}` - Generic range (with fieldName, min, max placeholders)

**Example message.properties entry**:
```properties
# Meal-specific validations
validation.meal.photo.notblank=식사 사진은 필수입니다.
validation.meal.mealType.notnull=식사 유형은 필수입니다.
validation.meal.memo.size.max=식사 메모는 200자 이내로 입력해야 합니다.

# Reaction-specific validations
validation.reaction.digestionLevel.notnull=소화 상태는 필수입니다.
validation.reaction.digestionLevel.range=소화 상태는 1~5 사이여야 합니다.
validation.reaction.fullnessLevel.notnull=포만감은 필수입니다.
validation.reaction.fullnessLevel.range=포만감은 1~5 사이여야 합니다.
validation.reaction.energyLevel.notnull=에너지 레벨은 필수입니다.
validation.reaction.energyLevel.range=에너지 레벨은 1~5 사이여야 합니다.
```

### API Response Format

All API responses follow `BaseResponse` structure:

**Success Response**:
```json
{
  "success": true,
  "timestamp": "2025-02-15T10:30:00",
  "data": {
    "id": 1,
    "mealType": "LUNCH",
    ...
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "timestamp": "2025-02-15T10:30:00",
  "error": {
    "code": "MEAL_NOT_FOUND",
    "message": "식사를 찾을 수 없습니다",
    "details": null
  }
}
```

**Paginated Response**:
```json
{
  "success": true,
  "timestamp": "2025-02-15T10:30:00",
  "data": {
    "items": [...],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5
    }
  }
}
```

### Database Schema

**Schema Management**: Flyway migrations in `src/main/resources/db/migration/`

**Key Tables**:
- `members`: User accounts with OAuth support
- `member_settings`: User notification preferences
- `member_withdrawals`: Withdrawal history
- `meals`: Meal records with photos
- `meal_analyses`: AI food analysis results
- `foods`: Food master data with avg scores
- `food_member_stats`: Per-user food statistics
- `reactions`: Post-meal body reactions
- `notifications`: Push notification history

**Indexes**: Comprehensive indexes on foreign keys, timestamps, and query patterns

**Triggers**: Auto-update `updated_at` timestamp

### Configuration Files

**application.yaml** (base):
- JPA/Hibernate settings
- Flyway migration
- Spring Batch configuration (disabled by default)
- Task execution pools (Virtual Threads)
- Spring Security JWT configuration
- SpringDoc OpenAPI settings
- Logging patterns
- File upload settings (10MB limit)
- **MessageSource configuration** for validation messages (spring.messages)

**application-local.yaml** (local dev):
- Local PostgreSQL connection
- Local Redis
- Mock AWS S3 (LocalStack)
- Debug logging

**application-prod.yaml** (production):
- RDS PostgreSQL
- ElastiCache Redis
- AWS S3
- Firebase FCM
- Production logging

### Testing

**Unit Tests** (Service layer):
- `@ExtendWith(MockitoExtension.class)`
- Mock domain Readers/Writers
- Test business logic in isolation

**Integration Tests** (end-to-end):
- `@SpringBootTest`
- Testcontainers for PostgreSQL
- Full context loading
- Test API endpoints

**Test Location**: `src/test/java/com/mymealserver/` (mirrors main package structure)

**Test Coverage**:
- Auth: AuthService, OAuthService, TokenService, TokenBlacklistService tests
- Meal: MealService tests
- Profile: ProfileService, StatisticsService, BodyPatternService tests
- Ranking: RankingService tests
- Calendar: CalendarService tests
- Notification: NotificationService tests
- Common: ArgumentResolver, Configuration tests
- Fixtures: OAuthFixture, ProfileFixture, MemberFixture, TokenFixture

---

## API Endpoints

### Authentication API (`/api/v1/auth`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| POST | `/register` | Email registration |
| POST | `/login` | Email login |
| POST | `/oauth` | Social login (Google, Naver, Kakao) |
| POST | `/refresh` | Token refresh |
| POST | `/logout` | Logout (blacklist token) |
| DELETE | `/withdraw` | Account withdrawal |

### Meal API (`/api/v1/meals`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| POST | `/meals` | Create meal with photo upload + AI analysis |
| GET | `/meals` | List meals (pagination/filtering) |
| GET | `/meals/{id}` | Get meal details (includes AI analysis) |
| DELETE | `/meals/{id}` | Delete meal |
| POST | `/meals/{id}/photo` | Retake photo (re-trigger AI analysis) |

### Reaction API (`/api/v1/meals/{id}/reactions`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| POST | `/reactions` | Record post-meal reaction |
| GET | `/reactions` | Get reactions for specific meal |
| GET | `/reactions/statistics` | Get reaction statistics |

### Profile API (`/api/v1/profile`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/profile` | Get user profile |
| PUT | `/profile` | Update user profile |
| GET | `/statistics` | Get user statistics |
| GET | `/patterns` | Get body pattern analysis |

### Ranking API (`/api/v1/ranking`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/ranking/best-foods` | Best foods ranking |
| GET | `/ranking/worst-foods` | Worst foods ranking |
| GET | `/ranking/best-days` | Best meal days ranking |
| GET | `/ranking/worst-days` | Worst meal days ranking |

### Calendar API (`/api/v1/calendar`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/calendar/daily` | Daily calendar data |
| GET | `/calendar/monthly` | Monthly calendar data |
| GET | `/calendar/monthly-summary` | Monthly summary |

### Recommendation API (`/api/v1/recommendations`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/recommendations` | Get personalized meal recommendations |
| GET | `/recommendations/schedule` | Get recommendation schedule |
| POST | `/recommendations/feedback` | Submit recommendation feedback |

### Settings API (`/api/v1/settings`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/settings` | Get user settings |
| PUT | `/settings` | Update user settings |

### Notification API (`/api/v1/notifications`)
| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/notifications` | Get notification list |
| PUT | `/notifications/settings` | Update notification settings |

**Total**: 45 API endpoints across 9 controllers

---

## External Service Integration

### AWS S3 (File Storage)
- **Purpose**: Meal photo storage, profile images
- **SDK**: AWS SDK for Java v2 (2.31.0)
- **Config**: `AwsS3Config.java`
- **Service**: `FileStorageService`
- **Region**: ap-northeast-2 (Seoul)
- **File Size Limit**: 10MB

### Firebase FCM (Push Notifications)
- **Purpose**: Meal reminders, reaction reminders, recommendations
- **SDK**: Firebase Admin SDK 9.7.0
- **Config**: `FirebaseConfig.java`
- **Service**: `FcmNotificationService`
- **Notification Types**:
  - RECOMMENDATION: Meal recommendations
  - REACTION_REMINDER: Remind to record reactions
  - MEAL_REMINDER: Meal time reminders

### OAuth2 Providers (Social Login)

**Google**:
- **Service**: `GoogleOAuthService`
- **Client**: `GoogleApiClient`
- **Scopes**: email, profile
- **Endpoints**: Token exchange, user info retrieval

**Naver**:
- **Service**: `NaverOAuthService`
- **Client**: `NaverApiClient`
- **Scopes**: email, profile
- **Endpoints**: Token exchange, user info retrieval

**Kakao**:
- **Service**: `KakaoOAuthService`
- **Client**: `KakaoApiClient`
- **Scopes**: account_email, profile_nickname
- **Endpoints**: Token exchange, user info retrieval

**Factory Pattern**: `OAuthServiceFactory` routes to appropriate provider based on `ProviderType`

### AI Services (Future)
- **Vision AI**: Google Gemini 2.0 Flash (food image analysis)
- **Embeddings**: OpenAI Embeddings (food vectorization)
- **Vector DB**: pgvector (similarity search)
- **Framework**: Spring AI 1.0.0-M4 (prepared, commented out)

---

## Git Commit Guidelines

### Commit Message Rules

- **DO NOT** include `Co-Authored-By: Claude Sonnet` or any AI attribution
- Use conventional commits format: `type(scope): description`
- Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `style`
- Keep subject line under 72 characters
- Write body in Korean for this project
- Reference issues if applicable: `#123`

### Commit Examples

```
feat(auth): 소셜 로그인 기능 구현

- Google, Naver, Kakao OAuth2 연동
- JWT 토큰 발급/갱신 로직 추가
- UserDetailsService 구현

fix(meal): 식사 삭제 시 연관된 반응 데이터 미삭제 버그 수정

Reaction 엔티티에 ON DELETE CASCADE 추가로 해결

refactor(domain): ReactionReader/Writer 분리

CQRS 패턴 적용하여 조회/명령 로직 분리
테스트 커버리지 85% 달성
```

---

## Common Development Tasks

### Add New Entity

1. Create entity class in `entity/`
2. Extend `SoftDeletable` if needed
3. Create repository interface in `repository/`
4. Add Flyway migration for schema
5. Create Reader/Writer in `domain/{entity}/`
6. Create service in `service/{feature}/`
7. Create controller and DTOs in `controller/` and `api/{feature}/dto/`

### Add New API Endpoint

1. Create DTO in `api/{feature}/dto/`
2. Add validation annotations with **message keys** (not hardcoded messages)
3. **Add validation message keys** to `src/main/resources/messages.properties`
4. Add method to service (or create new service)
5. Add controller method with proper mapping
6. Return `SuccessResponse<T>` or `ErrorResponse`
7. Update Swagger annotations

**Validation Message Addition**:
When adding new validation, add entries to `messages.properties`:
```properties
# Domain-specific validations
validation.{domain}.{field}.{constraint}=Message here
```

### Add External API Integration

1. Add dependency to `build.gradle`
2. Create configuration class in `config/`
3. Create client service in `service/{feature}/client/`
4. Add DTOs for request/response in `client/dto/`
5. Add properties to `application.yaml`
6. Handle errors with `BusinessException`

---

## Troubleshooting

### Build Issues
- **Clean build**: `./gradlew clean build`
- **Refresh dependencies**: `./gradlew build --refresh-dependencies`
- **Check Java version**: `java -version` (must be 21)

### Database Issues
- **Flyway baseline**: If migrating existing DB, set `flyway.baseline-on-migrate=true`
- **Reset schema**: Delete Flyway rows from `flyway_schema_history` table
- **Check connection**: Verify PostgreSQL is running on port 5432

### Runtime Issues
- **Profile active**: Ensure `--spring.profiles.active=local` for local dev
- **Port conflicts**: Change `server.port` in `application.yaml` if 8080 is in use
- **Memory**: Increase heap with `-Xmx2g` if needed

---

## Project Documentation

Additional documentation available in `.docs/`:

- **Architecture**: `.docs/architecture/tech-stack.md`, `.docs/architecture/package-structure.md`
- **Features**: `.docs/feature/feature.md`, `.docs/feature/feature-list.md`
- **Database**: `.docs/db/schema.sql`, `.docs/db/datamodel.md`
- **API**: `.docs/api/mymeal-api.yaml`, `.docs/api/api-list.md`
- **Planning**: `.docs/plan/develop-plan.md`, `.docs/plan/linear-milestones.md`

---

## Key Principles

1. **Layer Separation**: Controller → Service → Domain → Repository
2. **Reader/Writer Pattern**: Separate query and command operations
3. **Soft Delete**: Never hard-delete data (use `deleted_at`)
4. **Transaction Boundaries**: Explicit `@Transactional` on service methods
5. **Validation**: Use `@Valid` in controllers, **centralized messages in `messages.properties`**
6. **Error Handling**: Throw `BusinessException` with `ErrorCode`, handle in `GlobalExceptionHandler`
7. **API Consistency**: Always return `BaseResponse<T>` format
8. **Database Safety**: Use Flyway migrations, never manual schema changes
9. **Testing**: Write unit tests for services, integration tests for APIs
10. **Documentation**: Update Swagger annotations when adding/modifying endpoints
11. **Validation Messages**: Never hardcode - always use `{key}` format referencing `messages.properties`
12. **Authentication**: Use `@AuthenticatedMember` annotation for injecting authenticated users
