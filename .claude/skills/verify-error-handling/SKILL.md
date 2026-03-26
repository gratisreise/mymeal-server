---
name: verify-error-handling
description: 예외 처리, 에러 응답, 로깅 전략의 일관성을 검증합니다. 에러 처리 로직 수정 후 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 파일 또는 에러 유형]"
---

# 에러 처리 검증

## 목적

백엔드 애플리케이션의 에러 처리 품질을 검증합니다:

1. **예외 처리** — try-catch로 모든 예외 포착
2. **에러 응답** — 일관된 에러 응답 포맷
3. **로깅** — 적절한 에러 로깅 및 추적
4. **복구 전략** — graceful degradation 구현
5. **사용자 피드백** — 명확한 에러 메시지 전달

## 실행 시점

- 에러 처리 로직을 추가/수정한 후
- 새로운 API 엔드포인트 추가 후
- 외부 서비스 연동 코드 작성 후
- Pull Request 생성 전
- 프로덕션 에러 분석 시

## 워크플로우

### Step 1: try-catch 커버리지 확인

**검사:** 메서드에 에러 처리가 되어있는지 확인.

```bash
# try-catch 패턴 검색
grep -rn "try\s*{" src/main/java/ --include="*.java" | head -30
grep -rn "catch\s*(" src/main/java/ --include="*.java"

# throws 선언 확인
grep -rn "throws\s\+" src/main/java/ --include="*.java"
```

**위반 사례:**
```java
// 위험! 예외가 호출자로 전파됨
public User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
}
```

**PASS 기준:**
```java
// 안전 - 커스텀 예외 사용
public User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

// 또는 서비스 레이어에서 래핑
public User getUser(Long id) {
    try {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    } catch (Exception e) {
        log.error("Failed to get user with id: {}", id, e);
        throw new ServiceException("사용자 조회에 실패했습니다", e);
    }
}
```

### Step 2: 글로벌 에러 핸들러 확인

**검사:** 포착되지 않은 예외를 처리하는 글로벌 핸들러 존재 여부.

```bash
# @ControllerAdvice, @ExceptionHandler 검색
grep -rn "@ControllerAdvice\|@RestControllerAdvice\|@ExceptionHandler" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("USER_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of("VALIDATION_ERROR", "입력값이 올바르지 않습니다", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "서버 오류가 발생했습니다"));
    }
}
```

### Step 3: 커스텀 예외 클래스 검증

**검사:** 비즈니스 에러를 위한 커스텀 예외 클래스 사용 여부.

```bash
# 커스텀 예외 클래스 검색
grep -rn "class.*Exception\|extends RuntimeException\|extends Exception" src/main/java/ --include="*.java"
ls -la src/main/java/**/exception/ 2>/dev/null
ls -la src/main/java/**/error/ 2>/dev/null
```

**PASS 기준:**
```java
// 비즈니스 예외 베이스 클래스
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}

// 구체적인 예외 클래스
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "사용자를 찾을 수 없습니다: " + userId, HttpStatus.NOT_FOUND);
    }
}

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }
}

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
```

### Step 4: 에러 로깅 품질 확인

**검사:** 로그에 충분한 컨텍스트가 포함되어 있는지 확인.

```bash
# 로깅 패턴 검색
grep -rn "log\.\|logger\.\|Logger\|@Slf4j" src/main/java/ --include="*.java" | grep -i "error\|warn"
```

**위반 사항:**
- `e.printStackTrace()` — 표준 에러로 출력됨
- `log.error("Error occurred")` — 에러 객체 없음
- `System.out.println(error)` — 적절한 로깅 프레임워크 미사용

**PASS 기준:**
```java
@Slf4j
@Service
public class OrderService {

    public Order createOrder(OrderRequest request) {
        try {
            // 비즈니스 로직
            return orderRepository.save(order);
        } catch (DataAccessException e) {
            log.error("Failed to create order - userId: {}, request: {}",
                request.getUserId(), request, e);
            throw new ServiceException("주문 생성에 실패했습니다", e);
        }
    }

    public void processPayment(Long orderId) {
        log.info("Processing payment for order: {}", orderId);
        try {
            // 결제 처리
            log.debug("Payment completed for order: {}", orderId);
        } catch (PaymentException e) {
            log.error("Payment failed for order: {}, reason: {}",
                orderId, e.getReason(), e);
            throw e;
        }
    }
}
```

### Step 5: 외부 서비스 에러 처리

**검사:** 외부 API/DB 호출에 대한 에러 처리.

```bash
# 외부 서비스 호출 패턴 검색
grep -rn "RestTemplate\|WebClient\|HttpClient\|FeignClient" src/main/java/ --include="*.java"
grep -rn "repository\.\|jdbcTemplate\|namedParameterJdbcTemplate" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
@Service
@Slf4j
public class ExternalApiService {

    private final WebClient webClient;

    public ExternalApiService(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("https://external-api.com")
            .build();
    }

    public ExternalResponse callExternalApi(RequestData data) {
        try {
            return webClient.post()
                .uri("/api/endpoint")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(ExternalResponse.class)
                .timeout(Duration.ofSeconds(5))
                .block();
        } catch (WebClientRequestException e) {
            log.error("External API connection failed", e);
            throw new ExternalServiceUnavailableException("외부 서비스에 연결할 수 없습니다");
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RateLimitExceededException("요청 한도를 초과했습니다");
            }
            log.error("External API returned error: {}", e.getStatusCode(), e);
            throw new ExternalServiceException("외부 서비스 오류가 발생했습니다");
        }
    }
}
```

### Step 6: 에러 응답 일관성 확인

**검사:** 모든 에러 응답이 동일한 포맷을 따르는지 확인.

```bash
# 에러 응답 DTO 확인
grep -rn "ErrorResponse\|ApiError\|ErrorDetail" src/main/java/ --include="*.java"
ls -la src/main/java/**/dto/response/*Error*.java 2>/dev/null
```

**PASS 기준:**
```java
// 일관된 에러 응답 포맷
@Data
@Builder
public class ErrorResponse {
    private boolean success = false;
    private ErrorDetail error;
    private String requestId;
    private LocalDateTime timestamp;

    @Data
    @AllArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private List<FieldError> details;
    }

    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
            .error(new ErrorDetail(code, message, null))
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(String code, String message, List<FieldError> details) {
        return ErrorResponse.builder()
            .error(new ErrorDetail(code, message, details))
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

## 결과 출력 형식

```markdown
## 에러 처리 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| try-catch 커버리지 | PASS/FAIL | N개 |
| 글로벌 핸들러 | PASS/FAIL | N개 |
| 커스텀 에러 | PASS/FAIL | N개 |
| 로깅 품질 | PASS/FAIL | N개 |
| 외부 서비스 | PASS/FAIL | N개 |
| 응답 일관성 | PASS/FAIL | N개 |

### 발견된 이슈

| 파일 | 라인 | 문제 | 권장 수정 |
|------|------|------|-----------|
| `src/.../UserService.java:45` | try-catch 없음 | 메서드에 에러 처리 추가 |
| `src/.../OrderController.java:120` | 로깅 부족 | 에러 컨텍스트 추가 |
```

---

## 예외사항

1. **의도된 예외** — 비즈니스 로직에서 의도적으로 던지는 예외
2. **테스트 코드** — 테스트에서의 에러 시나리오
3. **초기화 코드** — 앱 시작 시 에러는 즉시 종료가 적절
4. **단순 스크립트** — 일회성 스크립트는 간단한 처리 가능
5. **Optional 반환** — null 대신 Optional을 사용하는 경우

## Related Files

| File | Purpose |
|------|---------|
| `src/main/java/**/exception/**/*.java` | 커스텀 예외 클래스 |
| `src/main/java/**/handler/*ExceptionHandler.java` | 글로벌 에러 핸들러 |
| `src/main/java/**/dto/response/*Error*.java` | 에러 응답 DTO |
| `src/main/java/**/service/**/*.java` | 서비스 레이어 |
| `src/main/resources/logback-spring.xml` | 로깅 설정 |
