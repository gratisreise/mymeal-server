---
name: verify-database
description: 데이터베이스 쿼리 성능, SQL 인젝션 방어, N+1 문제를 검증합니다. DB 쿼리/마이그레이션 수정 후 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 DB 파일 또는 쿼리 패턴]"
---

# 데이터베이스 검증

## 목적

데이터베이스 관련 코드의 보안과 성능을 검증합니다:

1. **SQL 인젝션 방어** — 파라미터화 쿼리 사용 여부
2. **N+1 쿼리 문제** — 불필요한 반복 쿼리 탐지
3. **인덱스 활용** — 쿼리 성능 최적화
4. **트랜잭션 관리** — ACID 원칙 준수
5. **마이그레이션 안전성** — 무손실 스키마 변경

## 실행 시점

- 데이터베이스 쿼리를 작성/수정한 후
- Entity 모델을 변경한 후
- 마이그레이션 파일을 생성한 후
- 성능 이슈가 보고되었을 때
- Pull Request 생성 전

## 워크플로우

### Step 1: SQL 인젝션 취약점 탐지

**검사:** 문자열 결합으로 쿼리를 구성하는지 확인.

```bash
# 위험한 패턴 검색 (Native Query)
grep -rn "nativeQuery\s*=\s*true" src/main/java/ --include="*.java"
grep -rn "createQuery\|createNativeQuery" src/main/java/ --include="*.java"

# MyBatis 사용 시
grep -rn "\$\{" src/main/resources/mapper/ --include="*.xml"
```

**위반 사례:**
```java
// 위험!
@Query(value = "SELECT * FROM users WHERE id = " + userId, nativeQuery = true)
User findById(String userId);

// MyBatis에서도 위험
// SELECT * FROM users WHERE name = '${name}'
```

**PASS 기준:**
```java
// 안전 - JPQL 파라미터 바인딩
@Query("SELECT u FROM User u WHERE u.id = :id")
User findById(@Param("id") Long id);

// 안전 - Native Query 파라미터 바인딩
@Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
User findByIdNative(@Param("id") Long id);

// MyBatis 안전한 방식
// SELECT * FROM users WHERE name = #{name}
```

### Step 2: N+1 쿼리 문제 탐지

**검사:** 연관 관계 조회 시 N+1 문제가 발생하는지 확인.

```bash
# 연관 관계 매핑 확인
grep -rn "@OneToMany\|@ManyToOne\|@ManyToMany\|@OneToOne" src/main/java/ --include="*.java"

# FETCH JOIN 사용 여부
grep -rn "JOIN FETCH\|EntityGraph\|@NamedEntityGraph" src/main/java/ --include="*.java"
```

**위반 사례:**
```java
// N+1 문제 발생!
@Entity
public class Order {
    @ManyToOne
    private User user;  // 지연 로딩 시 N+1 발생
}

// 조회 시
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    order.getUser().getName();  // 각 order마다 쿼리 실행
}
```

**PASS 기준:**
```java
// 해결 1: FETCH JOIN
@Query("SELECT o FROM Order o JOIN FETCH o.user")
List<Order> findAllWithUser();

// 해결 2: EntityGraph
@EntityGraph(attributePaths = {"user"})
List<Order> findAll();

// 해결 3: Batch Size 설정
@BatchSize(size = 100)
@ManyToOne
private User user;
```

### Step 3: 인덱스 사용 검증

**검사:** WHERE, JOIN, ORDER BY 컬럼에 인덱스가 있는지 확인.

```bash
# 인덱스 어노테이션 확인
grep -rn "@Index\|@Table.*indexes\|@Column.*unique" src/main/java/ --include="*.java"

# Flyway/Liquibase 마이그레이션 확인
grep -rn "CREATE INDEX\|CREATE UNIQUE INDEX" src/main/resources/db/migration/ --include="*.sql"
```

**PASS 기준:**
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_status", columnList = "status")
})
public class User {
    @Column(unique = true)
    private String email;

    private String status;
}
```

### Step 4: 트랜잭션 처리 검증

**검사:** 여러 쿼리가 원자적으로 처리되는지 확인.

```bash
# 트랜잭션 패턴 검색
grep -rn "@Transactional" src/main/java/ --include="*.java"

# 트랜잭션 설정 확인
grep -rn "isolation\|propagation\|readOnly\|rollbackFor" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
// 올바른 트랜잭션
@Service
@Transactional(readOnly = true)
public class OrderService {

    @Transactional
    public Order createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.save(new Order(user));
        orderItemRepository.saveAll(order.getItems());

        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPayment(Long orderId) {
        // 결제 처리 로직
    }
}
```

### Step 5: 마이그레이션 안전성 검증

**검사:** 파괴적 변경에 롤백이 있는지 확인.

```bash
# Flyway 마이그레이션 파일 확인
ls -la src/main/resources/db/migration/

# 파괴적 명령어 검색
grep -rn "DROP\|DELETE\|TRUNCATE" src/main/resources/db/migration/ --include="*.sql"

# Liquibase 사용 시
ls -la src/main/resources/db/changelog/
```

**PASS 기준:**
```
✓ Flyway: V{version}__{description}.sql 형식 준수
✓ 모든 DROP 전에 데이터 백업 확인
✓ NOT NULL 제약 추가 전 기본값 설정
✓ 대용량 테이블 변경 시 배치 처리
✓ 버전 네이밍 일관성 유지
```

## 결과 출력 형식

```markdown
## 데이터베이스 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| SQL 인젝션 | PASS/FAIL | N개 |
| N+1 쿼리 | PASS/FAIL | N개 |
| 인덱스 | PASS/FAIL | N개 |
| 트랜잭션 | PASS/FAIL | N개 |
| 마이그레이션 | PASS/FAIL | N개 |

### 발견된 이슈

| 파일 | 라인 | 문제 | 심각도 |
|------|------|------|--------|
| `src/.../UserRepository.java:45` | SQL 인젝션 위험 | CRITICAL |
| `src/.../OrderService.java:120` | N+1 쿼리 의심 | HIGH |
```

---

## 예외사항

1. **읽기 전용 쿼리** — 단순 조회는 트랜잭션 readOnly=true
2. **NoSQL** — MongoDB 등은 SQL 인젝션 대신 다른 검증 필요
3. **배치 작업** — 대량 처리는 별도 트랜잭션 전략
4. **로깅용 쿼리** — 감사 로그는 트랜잭션에서 제외 가능
5. **캐시된 쿼리** — Redis 등 캐시 활용 시 DB 부하 감소

## Related Files

| File | Purpose |
|------|---------|
| `src/main/java/**/entity/**/*.java` | JPA Entity |
| `src/main/java/**/repository/**/*.java` | Repository 레이어 |
| `src/main/resources/db/migration/**` | Flyway 마이그레이션 |
| `src/main/resources/db/changelog/**` | Liquibase 변경 로그 |
| `src/main/resources/mapper/**` | MyBatis Mapper XML |
| `src/main/resources/application.yml` | DB 설정 |
