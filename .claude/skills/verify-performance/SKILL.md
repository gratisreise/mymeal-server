---
name: verify-performance
description: 캐싱, 커넥션 풀, 비동기 처리 등 성능 최적화를 검증합니다. 성능 관련 코드 수정 후 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 파일 또는 성능 영역]"
---

# 성능 검증

## 목적

백엔드 애플리케이션의 성능 최적화를 검증합니다:

1. **캐싱 전략** — Redis, 로컬 캐시 활용
2. **커넥션 풀** — DB, HTTP 커넥션 재사용
3. **비동기 처리** — 병렬 실행, 논블로킹 I/O
4. **메모리 관리** — 누수 방지, 효율적 할당
5. **쿼리 최적화** — 효율적인 데이터 조회

## 실행 시점

- 성능 관련 코드를 수정한 후
- 캐싱 로직을 추가/변경한 후
- 대용량 데이터 처리 코드 작성 후
- Pull Request 생성 전
- 성능 저하 이슈 발생 시

## 워크플로우

### Step 1: 캐싱 구현 확인

**검사:** 반복 조회 데이터에 캐싱이 적용되었는지 확인.

```bash
# Spring Cache 어노테이션 검색
grep -rn "@Cacheable\|@CacheEvict\|@CachePut\|@Caching" src/main/java/ --include="*.java"

# Redis 캐시 사용 확인
grep -rn "RedisTemplate\|StringRedisTemplate\|@EnableRedisCaching" src/main/java/ --include="*.java"

# Caffeine/Ehcache 로컬 캐시 확인
grep -rn "Caffeine\|Ehcache\|@EnableCaching" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
// Spring Cache 적용
@Service
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        log.info("User cache cleared");
    }
}

// Redis 직접 사용
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public User getUserWithCache(Long id) {
        String cacheKey = "user:" + id;
        User cached = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        User user = userRepository.findById(id).orElseThrow(...);
        redisTemplate.opsForValue().set(cacheKey, user, Duration.ofHours(1));
        return user;
    }
}
```

**캐싱 권장 대상:**
- 설정 데이터
- 사용자 권한 정보
- 참조 테이블 데이터
- 계산 비용이 높은 결과

### Step 2: 커넥션 풀 설정 확인

**검사:** DB, HTTP 커넥션 풀이 적절히 설정되었는지 확인.

```bash
# HikariCP 설정 확인
grep -rn "hikari\|maximum-pool-size\|minimum-idle\|connection-timeout" src/main/resources/

# application.yml 설정 확인
cat src/main/resources/application.yml | grep -A10 "datasource\|hikari"
```

**PASS 기준:**
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: MyAppPool

  data:
    redis:
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

```java
// WebClient 커넥션 풀
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("custom")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(20))
            .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
```

### Step 3: 비동기 병렬 처리 확인

**검사:** 독립적인 작업이 병렬로 실행되는지 확인.

```bash
# @Async 사용 확인
grep -rn "@Async\|@EnableAsync\|CompletableFuture" src/main/java/ --include="*.java"

# 병렬 스트림 사용 확인
grep -rn "parallelStream\|parallel()" src/main/java/ --include="*.java"

# Reactor/WebFlux 사용 확인
grep -rn "Flux\|Mono\|subscribeOn\|publishOn" src/main/java/ --include="*.java"
```

**위반 사례:**
```java
// 순차 실행 (느림)
public OrderDetail getOrderDetail(Long orderId) {
    Order order = orderRepository.findById(orderId);
    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
    User user = userRepository.findById(order.getUserId());
    Payment payment = paymentRepository.findByOrderId(orderId);
    return new OrderDetail(order, items, user, payment);
}
```

**PASS 기준:**
```java
// 병렬 실행 (빠름) - @Async
@Service
public class OrderService {

    @Async
    public CompletableFuture<Order> getOrderAsync(Long orderId) {
        return CompletableFuture.completedFuture(orderRepository.findById(orderId));
    }

    @Async
    public CompletableFuture<List<OrderItem>> getItemsAsync(Long orderId) {
        return CompletableFuture.completedFuture(orderItemRepository.findByOrderId(orderId));
    }

    public OrderDetail getOrderDetail(Long orderId) {
        CompletableFuture<Order> orderFuture = getOrderAsync(orderId);
        CompletableFuture<List<OrderItem>> itemsFuture = getItemsAsync(orderId);

        return CompletableFuture.allOf(orderFuture, itemsFuture)
            .thenApply(v -> {
                Order order = orderFuture.join();
                List<OrderItem> items = itemsFuture.join();
                return new OrderDetail(order, items);
            })
            .join();
    }
}

// 또는 Reactor 사용
public Mono<OrderDetail> getOrderDetail(Long orderId) {
    return Mono.zip(
        orderRepository.findById(orderId),
        orderItemRepository.findByOrderId(orderId).collectList(),
        (order, items) -> new OrderDetail(order, items)
    );
}
```

### Step 4: 메모리 누수 패턴 탐지

**검사:** 메모리 누수 가능성이 있는 패턴 확인.

```bash
# 정적 컬렉션 확인 (주의 필요)
grep -rn "static.*Map\|static.*List\|static.*Set" src/main/java/ --include="*.java"

# ThreadLocal 사용 확인
grep -rn "ThreadLocal\|InheritableThreadLocal" src/main/java/ --include="*.java"

# 스케줄러 확인
grep -rn "@Scheduled\|ScheduledExecutorService\|Timer" src/main/java/ --include="*.java"
```

**위반 사항:**
- 무한히 증가하는 static Map/Set
- ThreadLocal 미해제
- 스케줄러 중복 실행

**PASS 기준:**
```java
// 제한된 캐시 (Caffeine)
@Component
public class LimitedCache {
    private final Cache<String, User> cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofHours(1))
        .build();
}

// 안전한 ThreadLocal 사용
public class RequestContext {
    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();

    public static void set(RequestContext ctx) {
        context.set(ctx);
    }

    public static RequestContext get() {
        return context.get();
    }

    public static void clear() {
        context.remove();  // 반드시 호출해야 함
    }
}

// 스케줄러
@Component
public class ScheduledTasks {

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        // 주기적 정리 작업
    }
}
```

### Step 5: 대용량 데이터 처리 확인

**검사:** 스트리밍, 배치 처리 사용 여부.

```bash
# Spring Batch 사용 확인
grep -rn "@Batch\|ItemReader\|ItemWriter\|Chunk" src/main/java/ --include="*.java"

# 페이징/커서 확인
grep -rn "Pageable\|PageRequest\|Slice\|Cursor" src/main/java/ --include="*.java"

# 스트리밍 확인
grep -rn "Stream<\|InputStream\|OutputStream\|BufferedReader" src/main/java/ --include="*.java"
```

**위반 사례:**
```java
// 메모리에 모두 로드 (위험)
public List<User> getAllUsers() {
    return userRepository.findAll();  // 대량 데이터 시 위험
}
```

**PASS 기준:**
```java
// 페이징 사용
public Page<User> getUsers(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
    return userRepository.findAll(pageable);
}

// 커서 기반 페이징
public Slice<User> getUsersCursor(Long lastId, int size) {
    Pageable pageable = PageRequest.of(0, size);
    return userRepository.findByIdGreaterThan(lastId, pageable);
}

// 스트리밍 (대용량 처리)
@Transactional(readOnly = true)
public void processAllUsers(Consumer<User> processor) {
    try (Stream<User> userStream = userRepository.streamAll()) {
        userStream.forEach(processor);
    }
}

// Spring Batch
@Bean
public Step userProcessingStep() {
    return stepBuilderFactory.get("userProcessingStep")
        .<User, User>chunk(100)
        .reader(userItemReader())
        .processor(userItemProcessor())
        .writer(userItemWriter())
        .build();
}
```

### Step 6: 응답 압축 확인

**검사:** 응답 데이터 압축이 적용되었는지 확인.

```bash
# 압축 설정 확인
grep -rn "compression\|gzip\|deflate" src/main/resources/
cat src/main/resources/application.yml | grep -A5 "compression"
```

**PASS 기준:**
```yaml
# application.yml
server:
  compression:
    enabled: true
    mime-types: application/json,text/html,text/xml,text/plain
    min-response-size: 1024
```

## 결과 출력 형식

```markdown
## 성능 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| 캐싱 전략 | PASS/FAIL | N개 |
| 커넥션 풀 | PASS/FAIL | N개 |
| 병렬 처리 | PASS/FAIL | N개 |
| 메모리 관리 | PASS/FAIL | N개 |
| 대용량 처리 | PASS/FAIL | N개 |
| 응답 압축 | PASS/FAIL | N개 |

### 발견된 이슈

| 파일 | 라인 | 문제 | 권장 수정 |
|------|------|------|-----------|
| `src/.../ReportService.java:45` | 순차 실행 | CompletableFuture로 병렬 처리 |
| `src/.../DatabaseConfig.java:10` | 풀 설정 없음 | HikariCP 설정 추가 |
```

---

## 예외사항

1. **실시간 데이터** — 항상 최신이어야 하는 데이터는 캐싱 부적절
2. **소량 데이터** — 캐싱 오버헤드가 이득보다 큰 경우
3. **일회성 작업** — 배치나 스크립트는 풀 불필요
4. **순차 의존성** — 이전 결과가 필요한 작업은 병렬 불가
5. **개발 환경** — 로컬 개발에서는 최적화 완화 가능

## Related Files

| File | Purpose |
|------|---------|
| `src/main/java/**/config/CacheConfig.java` | 캐시 설정 |
| `src/main/java/**/config/DatabaseConfig.java` | DB 커넥션 풀 설정 |
| `src/main/java/**/config/AsyncConfig.java` | 비동기 설정 |
| `src/main/java/**/service/**/*.java` | 서비스 레이어 |
| `src/main/resources/application.yml` | 애플리케이션 설정 |
