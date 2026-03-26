---
name: verify-security
description: 인증/인가, 민감 정보 노출, 입력 검증 등 보안 취약점을 검증합니다. 보안 관련 코드 수정 후 사용.
disable-model-invocation: true
argument-hint: "[선택사항: 특정 파일 또는 보안 영역]"
---

# 보안 검증

## 목적

백엔드 애플리케이션의 보안 취약점을 체계적으로 검증합니다:

1. **인증/인가** — 적절한 접근 제어 구현
2. **민감 정보 보호** — 비밀번호, API 키, 토큰 노출 방지
3. **입력 검증** — 모든 외부 입력에 대한 유효성 검사
4. **의존성 보안** — 알려진 취약점이 있는 패키지 탐지
5. **HTTPS/암호화** — 전송 및 저장 데이터 보호

## 실행 시점

- 인증/인가 로직을 수정한 후
- 사용자 입력을 처리하는 코드 작성 후
- 환경 변수나 설정 파일을 변경한 후
- 새로운 의존성을 추가한 후
- Pull Request 생성 전 (특히 보안 관련)
- 정기 보안 점검 시

## 워크플로우

### Step 1: 민감 정보 노출 탐지

**검사:** 하드코딩된 비밀번호, API 키, 토큰을 찾습니다.

```bash
# 민감 정보 패턴 검색
grep -rn "password\s*=\s*\".*\"\|apiKey\s*=\s*\".*\"\|secret\s*=\s*\".*\"" src/main/java/ --include="*.java"
grep -rn "-----BEGIN.*PRIVATE-----\|aws_access_key_id\|aws_secret_access_key" src/main/resources/

# 환경 변수 사용 없이 하드코딩된 경우
grep -rn "JWT_SECRET\|DATABASE_PASSWORD\|API_KEY" src/main/java/ --include="*.java" | grep -v "@Value\|Environment\|applicationConfig"
```

**위반 사례:**
```java
// 위험!
private String password = "admin123";
private String apiKey = "sk-abc123xyz";
private String dbUrl = "mysql://root:password@localhost/db";
```

**PASS 기준:**
```java
// 안전
@Value("${spring.datasource.password}")
private String dbPassword;

// 또는 Environment 사용
@Autowired
private Environment env;

String apiKey = env.getProperty("api.key");

// 또는 @ConfigurationProperties
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String jwtSecret;
    // getter, setter
}
```

### Step 2: 인증/인가 검증

**검사:** 보호된 엔드포인트에 인증/인가가 적용되었는지 확인.

```bash
# Spring Security 패턴 검색
grep -rn "@PreAuthorize\|@Secured\|@RolesAllowed\|hasRole\|hasAuthority" src/main/java/ --include="*.java"
grep -rn "@AuthenticationPrincipal\|Principal\|SecurityContext" src/main/java/ --include="*.java"

# SecurityConfig 확인
grep -rn "configure\|filterChain\|antMatchers\|requestMatchers" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
// Controller 레벨 보안
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('READ_USER')")
    public List<User> getUsers() { ... }
}

// SecurityConfig
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

### Step 3: 입력 검증 확인

**검사:** 모든 사용자 입력이 검증되는지 확인.

```bash
# Bean Validation 어노테이션 검색
grep -rn "@NotNull\|@NotBlank\|@Valid\|@Validated\|@Size\|@Pattern\|@Email" src/main/java/ --include="*.java"

# DTO/Request 클래스 확인
grep -rn "@RequestBody\|@PathVariable\|@RequestParam" src/main/java/ --include="*.java"
```

**위반 사례:**
```java
// 위험!
@PostMapping("/users")
public User createUser(@RequestBody UserRequest request) {
    // 검증 없이 바로 사용
    return userService.create(request);
}
```

**PASS 기준:**
```java
// 안전
@Data
public class UserRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2-50자여야 합니다")
    private String name;
}

@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest request) {
    return ResponseEntity.ok(userService.create(request));
}

// Controller 레벨 검증
@RestController
@Validated
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable @Min(1) Long id) {
        return userService.findById(id);
    }
}
```

### Step 4: 의존성 취약점 스캔

**검사:** 알려진 보안 취약점이 있는 패키지 탐지.

```bash
# Maven
./mvnw dependency-check:check 2>/dev/null
./mvnw org.owasp:dependency-check-maven:check 2>/dev/null

# Gradle
./gradlew dependencyCheckAnalyze 2>/dev/null

# 또는 Snyk 사용
snyk test 2>/dev/null
```

**PASS 기준:**
```
✓ Critical/High 취약점 0개
✓ Moderate 취약점은 평가 후 조치
✓ 최신 버전으로 업데이트된 의존성
```

### Step 5: CORS 및 보안 헤더 확인

**검사:** 적절한 보안 헤더가 설정되었는지 확인.

```bash
# Security Config에서 CORS 설정 확인
grep -rn "cors\|CorsConfiguration\|allowedOrigins\|allowedMethods" src/main/java/ --include="*.java"

# CSP, X-Frame-Options 등
grep -rn "Content-Security-Policy\|X-Frame-Options\|X-Content-Type-Options\|headers()" src/main/java/ --include="*.java"
```

**PASS 기준:**
```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(Customizer.withDefaults())
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://example.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);
        // ...
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

### Step 6: 비밀번호 저장 검증

**검사:** 비밀번호가 안전하게 해시되어 저장되는지 확인.

```bash
# BCrypt 등 암호화 사용 확인
grep -rn "BCryptPasswordEncoder\|PasswordEncoder\|Argon2PasswordEncoder\|SCryptPasswordEncoder" src/main/java/ --include="*.java"
grep -rn "encode\|matches" src/main/java/ --include="*.java" | grep -i password
```

**위반 사항:**
- 평문 비밀번호 저장
- MD5, SHA1 등 약한 해시 사용
- 솔트(salt) 미사용

**PASS 기준:**
```java
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserRequest request) {
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // 또는 더 강력한 Argon2
        // return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }
}
```

## 결과 출력 형식

```markdown
## 보안 검증 결과

| 검사 항목 | 상태 | 발견 이슈 |
|-----------|------|-----------|
| 민감 정보 노출 | PASS/FAIL | N개 |
| 인증/인가 | PASS/FAIL | N개 |
| 입력 검증 | PASS/FAIL | N개 |
| 의존성 취약점 | PASS/FAIL | N개 |
| 보안 헤더 | PASS/FAIL | N개 |
| 비밀번호 저장 | PASS/FAIL | N개 |

### 발견된 취약점

| 파일 | 라인 | 취약점 유형 | 심각도 |
|------|------|-------------|--------|
| `src/.../Config.java:15` | 하드코딩된 API 키 | CRITICAL |
| `src/.../AdminController.java:22` | 인증 우회 가능 | HIGH |
| `src/.../UserController.java:45` | 입력 검증 누락 | MEDIUM |
```

---

## 예외사항

1. **공개 API 키** — 프론트엔드에서 사용하는 공개 키 (예: Firebase)
2. **테스트 환경** — 테스트용 mock 데이터
3. **로그 파일** — 로그에 민감 정보가 없어야 함 (별도 규칙)
4. **공개 엔드포인트** — 인증 불필요한 공개 API
5. **레거시 시스템** — 점진적 마이그레이션 필요

## Related Files

| File | Purpose |
|------|---------|
| `src/main/java/**/config/SecurityConfig.java` | Spring Security 설정 |
| `src/main/java/**/controller/**/*.java` | Controller |
| `src/main/java/**/dto/request/**/*.java` | 요청 DTO (검증 포함) |
| `src/main/resources/application*.yml` | 환경 설정 |
| `pom.xml` / `build.gradle` | 의존성 정의 |
