package com.mymealserver.api.auth;


import com.mymealserver.api.auth.dto.request.LoginRequest;
import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.request.RefreshTokenRequest;
import com.mymealserver.api.auth.dto.request.RegisterRequest;
import com.mymealserver.api.auth.dto.request.WithdrawRequest;
import com.mymealserver.api.auth.dto.response.AuthResponse;
import com.mymealserver.api.auth.dto.response.RefreshResponse;
import com.mymealserver.api.auth.service.AuthService;
import com.mymealserver.api.auth.service.oauth.OAuthService;
import com.mymealserver.api.auth.service.oauth.OAuthServiceFactory;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthServiceFactory oAuthServiceFactory;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return SuccessResponse.toCreated(null);
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/oauth")
    public ResponseEntity<SuccessResponse<AuthResponse>> oauthLogin(
            @Valid @RequestBody OAuthRequest request
    ) {
        OAuthService oAuthService = oAuthServiceFactory.getOAuthService(request);
        AuthResponse response = oAuthService.authenticate(request);
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        RefreshResponse response = authService.reissueToken(request);
        return SuccessResponse.toOk(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @CurrentMember Long memberId,
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(memberId, request.refreshToken());
        return SuccessResponse.toOk(null);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<SuccessResponse<Void>> withdraw(
            @CurrentMember Long memberId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        authService.withdraw(memberId, request);
        return SuccessResponse.toOk(null);
    }
}
