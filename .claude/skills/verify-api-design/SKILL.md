---
name: verify-api-design
description: RESTful API 설계 원칙과 엔드포인트 명명 규칙을 검증합니다. API 엔드포인트 추가/수정 후, PR 전 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 API 파일 또는 경로]"
---

# API 설계 검증

## 목적

백엔드 API의 설계 품질과 일관성을 검증합니다:

1. **RESTful 원칙 준수** — 리소스 기반 URL, 올바른 HTTP 메서드 사용
2. **엔드포인트 명명 규칙** — kebab-case, 복수형 리소스, 버전 관리
3. **요청/응답 구조** — 일관된 응답 포맷, 적절한 상태 코드
4. **API 문서화** — OpenAPI/Swagger 스펙 준수 여부

## 실행 시점

- 새로운 API 엔드포인트를 추가한 후
- 기존 API를 수정하거나 리팩토링한 후
- Pull Request 생성 전
- API 버전 업그레이드 시

## 워크플로우

### Step 1: RESTful URL 패턴 검증

**검사:** 엔드포인트 URL이 RESTful 원칙을 따르는지 확인.

```bash
# Spring Controller에서 URL 패턴 검색
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping" src/main/java/ --include="*.java"

# JAX-RS 기반인 경우
grep -rn "@Path\|@GET\|@POST\|@PUT\|@DELETE" src/main/java/ --include="*.java"
```

**위반 사례:**
- URL에 동사 사용: `/getUsers`, `/createOrder`
- 단수형 리소스: `/user`, `/order` (복수형 권장)
- 캐밥케이스 미사용: `/user_profiles`, `/userProfiles`

**PASS 기준:**
```
✓ 리소스는 복수형 명사: /users, /orders, /products
✓ 계층 구조는 최대 3단계: /users/{id}/orders/{orderId}
✓ kebab-case 사용: /user-profiles, /order-items
✓ 버전은 URL prefix: /api/v1/users
```

### Step 2: HTTP 메서드 적절성 검증

**검사:** 각 엔드포인트에 올바른 HTTP 메서드가 사용되었는지 확인.

```bash
# 잘못된 메서드 사용 패턴 검색 (GET으로 리소스 생성 등)
grep -rn "@GetMapping.*create\|@GetMapping.*delete\|@GetMapping.*update" src/main/java/ --include="*.java"
```

**PASS 기준:**
```
✓ GET: 리소스 조회 (멱등성, 안전)
✓ POST: 리소스 생성
✓ PUT: 리소스 전체 교체 (멱등성)
✓ PATCH: 리소스 부분 수정
✓ DELETE: 리소스 삭제 (멱등성)
```

### Step 3: 응답 상태 코드 검증

**검사:** 적절한 HTTP 상태 코드가 반환되는지 확인.

```bash
grep -rn "HttpStatus\.\|ResponseEntity\.status\|\.status(" src/main/java/ --include="*.java"
grep -rn "@ResponseStatus" src/main/java/ --include="*.java"
```

**PASS 기준:**
```
✓ 200 OK: 성공적인 조회/수정
✓ 201 Created: 리소스 생성 성공
✓ 204 No Content: 삭제 성공
✓ 400 Bad Request: 잘못된 요청
✓ 401 Unauthorized: 인증 필요
✓ 403 Forbidden: 권한 없음
✓ 404 Not Found: 리소스 없음
✓ 422 Unprocessable Entity: 유효성 실패
✓ 500 Internal Server Error: 서버 에러
```

### Step 4: 일관된 응답 구조 검증

**검사:** API 응답이 일관된 구조를 따르는지 확인.

```bash
# 공통 응답 클래스 확인 (mymeal-server)
ls -la src/main/java/com/mymealserver/common/response/
# 예상 파일: BaseResponse.java, SuccessResponse.java, ErrorResponse.java, PageResponse.java

# 컨트롤러에서 응답 타입 확인
grep -rn "SuccessResponse\|ErrorResponse\|PageResponse\|ResponseEntity" src/main/java/com/mymealserver/ --include="*.java"
```

**PASS 기준:**
```java
// 추상 기본 응답 (BaseResponse)
public abstract class BaseResponse {
    private final boolean success;
    private final LocalDateTime timestamp;
}

// 성공 응답 (SuccessResponse extends BaseResponse)
public class SuccessResponse<T> extends BaseResponse {
    private final T data;

    // 정적 팩토리 메서드
    public static <T> ResponseEntity<SuccessResponse<T>> toOk(T data);
    public static <T> ResponseEntity<SuccessResponse<T>> toCreated(T data);
    public static <T> ResponseEntity<SuccessResponse<T>> toNoContent(T data);
    public static <T> ResponseEntity<SuccessResponse<T>> to(HttpStatus status, T data);
}

// 에러 응답 (ErrorResponse extends BaseResponse)
public class ErrorResponse extends BaseResponse {
    private final ErrorDetail error;

    public record ErrorDetail(
        String code,
        String message
    ) { }

    // 정적 팩토리 메서드
    public static ErrorResponse from(ErrorCode code);
    public static ErrorResponse from(String code, String message);
    public static ErrorResponse unknown(Exception ex);
}

// 페이지네이션 (PageResponse)
public class PageResponse<T> {
    private final List<T> data;
    private final Pagination pagination;

    public record Pagination(
        int currentPage,    // 1-based
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
    ) { }

    public static <T> PageResponse<T> from(Page<T> page);
}
```

**실제 응답 JSON 예시:**
```json
// 성공 응답
{
  "success": true,
  "timestamp": "2025-02-15T10:30:00",
  "data": { ... }
}

// 에러 응답
{
  "success": false,
  "timestamp": "2025-02-15T10:30:00",
  "error": {
    "code": "MEAL_NOT_FOUND",
    "message": "식사를 찾을 수 없습니다"
  }
}

// 페이지네이션 응답
{
  "data": [...],
  "pagination": {
    "currentPage": 1,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true
  }
}
```

## 결과 출력 형식

```markdown
## API 설계 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| RESTful URL | PASS/FAIL | N개 |
| HTTP 메서드 | PASS/FAIL | N개 |
| 상태 코드 | PASS/FAIL | N개 |
| 응답 구조 | PASS/FAIL | N개 |
```

---

## 예외사항

1. **GraphQL 엔드포인트** — REST 원칙 미적용
2. **WebSocket 엔드포인트** — 실시간 통신 패턴
3. **레거시 API** — 하위 호환성 유지
4. **내부 API** — 외부 노출 없는 내부 통신
5. **RPC 스타일** — gRPC, JSON-RPC 별도 규칙

## Related Files

| File | Purpose |
|------|---------|
| `src/main/java/com/mymealserver/controller/**/*.java` | Spring Controller |
| `src/main/java/com/mymealserver/api/**/*Controller.java` | API 핸들러 |
| `src/main/java/com/mymealserver/api/**/*Request.java` | 요청 DTO |
| `src/main/java/com/mymealserver/api/**/*Response.java` | 응답 DTO |
| `src/main/java/com/mymealserver/common/response/*.java` | 공통 응답 클래스 |
| `src/main/java/com/mymealserver/common/exception/ErrorCode.java` | 에러 코드 정의 |
| `src/main/resources/application.yaml` | API 설정 |
