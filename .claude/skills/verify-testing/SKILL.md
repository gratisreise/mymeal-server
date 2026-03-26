---
name: verify-testing
description: 단위 테스트, 통합 테스트, 테스트 커버리지를 검증합니다. 테스트 코드 작성 후 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 테스트 파일 또는 커버리지 대상]"
---

# 테스트 검증

## 목적

백엔드 애플리케이션의 테스트 품질을 검증합니다:

1. **테스트 커버리지** — 충분한 코드 커버리지
2. **단위 테스트 품질** — 격리성, 명확성, 반복성
3. **통합 테스트** — API 엔드포인트 테스트
4. **모킹 전략** — 외부 의존성 격리
5. **테스트 명명** — 명확한 테스트 의도 표현

## 실행 시점

- 새로운 테스트 코드를 작성한 후
- 기존 테스트를 수정한 후
- Pull Request 생성 전
- CI/CD 파이프라인 실행 시
- 리팩토링 전후

## 워크플로우

### Step 1: 테스트 커버리지 확인

**검사:** 코드 커버리지가 기준을 충족하는지 확인.

```bash
# JaCoCo 커버리지 실행 (Maven)
./mvnw clean test jacoco:report

# JaCoCo 커버리지 실행 (Gradle)
./gradlew test jacocoTestReport

# 커버리지 리포트 확인
cat target/site/jacoco/index.html 2>/dev/null || cat build/reports/jacoco/test/html/index.html 2>/dev/null

# 커버리지 설정 확인
grep -rn "jacoco\|coverage" pom.xml build.gradle
```

**PASS 기준:**
```
✓ 라인 커버리지: 80% 이상
✓ 분기 커버리지: 70% 이상
✓ 메서드 커버리지: 80% 이상
✓ 핵심 비즈니스 로직: 90% 이상
```

### Step 2: 단위 테스트 품질 확인

**검사:** AAA 패턴(Arrange-Act-Assert) 준수 여부.

```bash
# 테스트 파일 확인
ls -la src/test/java/**/*Test.java 2>/dev/null
ls -la src/test/java/**/*Tests.java 2>/dev/null

# 테스트 메서드 패턴 검색
grep -rn "@Test\|@DisplayName\|@ParameterizedTest" src/test/java/ --include="*.java" | head -20
```

**PASS 기준:**
```java
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("createUser 메서드")
    class CreateUser {

        @Test
        @DisplayName("유효한 데이터로 사용자 생성 시 성공한다")
        void shouldCreateUserWithValidData() {
            // Arrange
            UserRequest request = new UserRequest("test@example.com", "Test User");
            User expectedUser = new User(1L, request.getEmail(), request.getName());

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willReturn(expectedUser);

            // Act
            User result = userService.createUser(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(request.getEmail());
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 생성 시 예외를 던진다")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            UserRequest request = new UserRequest("existing@example.com", "Test User");
            given(userRepository.findByEmail(request.getEmail()))
                .willReturn(Optional.of(new User()));

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");
        }
    }
}
```

### Step 3: 테스트 명명 규칙 확인

**검사:** 테스트 이름이 명확한 의도를 표현하는지 확인.

```bash
# 테스트 이름 패턴 검색
grep -rn "@DisplayName\|void should\|void test\|void when" src/test/java/ --include="*.java"
```

**위반 사례:**
```java
// 나쁜 명명
@Test
void test1() { ... }

@Test
void testCreateUser() { ... }

@Test
void userTest() { ... }
```

**PASS 기준:**
```java
// 좋은 명명 - @DisplayName 사용
@Test
@DisplayName("이메일이 유효하지 않으면 ValidationException을 던진다")
void shouldThrowValidationExceptionWhenEmailIsInvalid() { ... }

@Test
@DisplayName("사용자가 존재하지 않으면 404를 반환한다")
void shouldReturn404WhenUserDoesNotExist() { ... }

@Test
@DisplayName("저장 전 비밀번호를 해시화한다")
void shouldHashPasswordBeforeSavingUser() { ... }
```

### Step 4: 모킹 전략 확인

**검사:** 외부 의존성이 적절히 모킹되었는지 확인.

```bash
# 모킹 패턴 검색
grep -rn "@Mock\|@MockBean\|@InjectMocks\|Mockito\|given\|when" src/test/java/ --include="*.java"

# BDD Mockito 사용 확인
grep -rn "given\|willReturn\|willThrow\|then\|should" src/test/java/ --include="*.java"
```

**PASS 기준:**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("결제 성공 시 주문이 완료되고 알림이 발송된다")
    void shouldCompleteOrderAndNotifyOnPaymentSuccess() {
        // given
        Order order = new Order(1L, OrderStatus.PENDING);
        PaymentResult paymentResult = new PaymentResult(true, "PAY-123");

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(paymentService.processPayment(any())).willReturn(paymentResult);
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        Order result = orderService.processOrder(1L);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        then(paymentService).should().processPayment(any());
        then(notificationService).should().sendOrderConfirmation(order);
    }

    @Test
    @DisplayName("결제 실패 시 주문이 취소되고 알림이 발송되지 않는다")
    void shouldCancelOrderOnPaymentFailure() {
        // given
        Order order = new Order(1L, OrderStatus.PENDING);
        PaymentResult paymentResult = new PaymentResult(false, null);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(paymentService.processPayment(any())).willReturn(paymentResult);

        // when
        Order result = orderService.processOrder(1L);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        then(notificationService).should(never()).sendOrderConfirmation(any());
    }
}
```

### Step 5: 통합 테스트 확인

**검사:** API 엔드포인트 통합 테스트 존재 여부.

```bash
# 통합 테스트 패턴 검색
grep -rn "@SpringBootTest\|@WebMvcTest\|@DataJpaTest\|@AutoConfigureMockMvc" src/test/java/ --include="*.java"
ls src/test/java/**/integration/ 2>/dev/null
```

**PASS 기준:**
```java
// Controller 통합 테스트
@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users - 사용자 생성")
    void shouldCreateUser() throws Exception {
        // given
        UserRequest request = new UserRequest("test@example.com", "Test User");
        User response = new User(1L, request.getEmail(), request.getName());

        given(userService.createUser(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.name").value("Test User"));
    }
}

// Repository 통합 테스트
@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회")
    void shouldFindByEmail() {
        // given
        User user = new User(null, "test@example.com", "Test");
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }
}
```

### Step 6: 에지 케이스 테스트 확인

**검사:** 예외 상황, 경계값 테스트 존재 여부.

```bash
# 에지 케이스 패턴 검색
grep -rn "@NullSource\|@EmptySource\|@ValueSource\|@MethodSource\|@CsvSource\|Exception\|error\|invalid\|empty" src/test/java/ --include="*.java"
```

**필수 테스트 케이스:**
- 빈 값, null 입력
- 유효하지 않은 형식
- 권한 없는 접근
- 리소스 없음
- 동시성 이슈
- 타임아웃

**PASS 기준:**
```java
@ParameterizedTest
@DisplayName("유효하지 않은 이메일로 생성 시 예외를 던진다")
@ValueSource(strings = {"", "invalid", "no-at-sign", "@no-local", "spaces in@email.com"})
void shouldThrowExceptionForInvalidEmail(String invalidEmail) {
    UserRequest request = new UserRequest(invalidEmail, "Test");

    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(ValidationException.class);
}

@ParameterizedTest
@NullAndEmptySource
@DisplayName("이름이 null이거나 비어있으면 예외를 던진다")
void shouldThrowExceptionForNullOrEmptyName(String name) {
    UserRequest request = new UserRequest("test@example.com", name);

    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(ValidationException.class);
}

@Test
@DisplayName("동시에 같은 이메일로 가입 시 하나만 성공한다")
void shouldAllowOnlyOneConcurrentRegistration() throws Exception {
    // given
    UserRequest request = new UserRequest("concurrent@example.com", "Test");
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // when
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                userService.createUser(request);
                successCount.incrementAndGet();
            } catch (DuplicateEmailException e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
    }
    latch.await(5, TimeUnit.SECONDS);

    // then
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failCount.get()).isEqualTo(threadCount - 1);
}
```

### Step 7: 테스트 격리성 확인

**검사:** 테스트 간 독립성 보장 여부.

```bash
# beforeEach/afterEach 패턴 검색
grep -rn "@BeforeEach\|@AfterEach\|@BeforeAll\|@AfterAll\|@DirtiesContext" src/test/java/ --include="*.java"
```

**PASS 기준:**
```java
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 각 테스트 전 초기 데이터 설정
        testUser = userRepository.save(new User("test@example.com", "Test User"));
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 데이터 정리
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자별 주문 조회")
    void shouldFindOrdersByUser() {
        // given
        orderRepository.save(new Order(testUser, OrderStatus.COMPLETED));
        orderRepository.save(new Order(testUser, OrderStatus.PENDING));

        // when
        List<Order> orders = orderRepository.findByUserId(testUser.getId());

        // then
        assertThat(orders).hasSize(2);
    }
}
```

## 결과 출력 형식

```markdown
## 테스트 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| 커버리지 | PASS/FAIL | 75% (기준: 80%) |
| 단위 테스트 품질 | PASS/FAIL | N개 |
| 테스트 명명 | PASS/FAIL | N개 |
| 모킹 전략 | PASS/FAIL | N개 |
| 통합 테스트 | PASS/FAIL | N개 |
| 에지 케이스 | PASS/FAIL | N개 |
| 테스트 격리 | PASS/FAIL | N개 |

### 커버리지 미달 파일

| 파일 | 커버리지 | 미커버 라인 |
|------|----------|-------------|
| `src/.../UserService.java` | 65% | 45-52, 78-85 |
| `src/.../Validator.java` | 70% | 12-18 |

### 개선 권장

| 테스트 파일 | 문제 | 권장 수정 |
|-------------|------|-----------|
| `UserServiceTest.java` | 모킹 없음 | Repository 모킹 추가 |
| `OrderTest.java` | 명명 불명확 | 구체적인 테스트 이름 사용 |
```

---

## 예외사항

1. **POJO/DTO** — 단순 데이터 클래스는 테스트 생략 가능
2. **프레임워크 코드** — 프레임워크 자체 기능은 테스트 불필요
3. **단순 CRUD** — 기본 CRUD는 통합 테스트로 충분
4. **일회성 스크립트** — 실행 스크립트는 테스트 선택적
5. **서드파티 라이브러리** — 외부 라이브러리는 모킹으로 처리

## Related Files

| File | Purpose |
|------|---------|
| `src/test/java/**/*Test.java` | 단위 테스트 |
| `src/test/java/**/*Tests.java` | 단위 테스트 (복수형) |
| `src/test/java/**/integration/**/*.java` | 통합 테스트 |
| `src/test/resources/` | 테스트 리소스 |
| `pom.xml` / `build.gradle` | 테스트 의존성 및 JaCoCo 설정 |
