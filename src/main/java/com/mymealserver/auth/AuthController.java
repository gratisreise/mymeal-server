package com.mymealserver.auth;

import com.mymealserver.auth.dto.AuthResponse;
import com.mymealserver.auth.dto.LoginRequest;
import com.mymealserver.auth.dto.OAuthRequest;
import com.mymealserver.auth.dto.RefreshTokenRequest;
import com.mymealserver.auth.dto.RegisterRequest;
import com.mymealserver.auth.dto.WithdrawRequest;
import com.mymealserver.auth.service.AuthService;
import com.mymealserver.auth.service.OAuthService;
import com.mymealserver.auth.service.TokenService;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 API")
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @Operation(summary = "이메일 회원가입", description = "이메일로 새로운 회원을 등록합니다.")
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration request for email: {}", request.email());
        authService.register(request);
        return SuccessResponse.toCreated(null);
    }

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request for email: {}", request.email());
        AuthResponse response = authService.login(request);
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/oauth/{provider}")
    @Operation(summary = "소셜 로그인", description = "소셜 로그인(Google, Naver, Kakao)을 수행합니다.")
    public ResponseEntity<SuccessResponse<AuthResponse>> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthRequest request
    ) {
        log.info("OAuth login request for provider: {}", provider);
        AuthResponse response = oAuthService.oauthLogin(provider, request);
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
    public ResponseEntity<SuccessResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Token refresh request");
        AuthResponse response = tokenService.refreshToken(request.refreshToken());
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @AuthenticationPrincipal Long memberId
    ) {
        log.info("Logout request for member: {}", memberId);
        authService.logout(memberId);
        return SuccessResponse.toOk(null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴합니다.")
    public ResponseEntity<SuccessResponse<Void>> withdraw(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        log.info("Withdrawal request for member: {}", memberId);
        authService.withdraw(memberId, request);
        return SuccessResponse.toOk(null);
    }
}
