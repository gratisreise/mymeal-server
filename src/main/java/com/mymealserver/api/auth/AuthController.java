package com.mymealserver.api.auth;

import com.mymealserver.api.auth.dto.request.LoginRequest;
import com.mymealserver.api.auth.dto.request.LogoutRequest;
import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.request.RefreshRequest;
import com.mymealserver.api.auth.dto.request.RegisterRequest;
import com.mymealserver.api.auth.dto.request.WithdrawRequest;
import com.mymealserver.api.auth.dto.response.LoginResponse;
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
      @Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return SuccessResponse.toCreated(null);
  }

  @PostMapping("/login")
  public ResponseEntity<SuccessResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    return SuccessResponse.toOk(authService.login(request));
  }

  @PostMapping("/oauth")
  public ResponseEntity<SuccessResponse<LoginResponse>> oauthLogin(
      @Valid @RequestBody OAuthRequest request) {
    OAuthService oAuthService = oAuthServiceFactory.getOAuthService(request.provider());
    return SuccessResponse.toOk(oAuthService.authenticate(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<SuccessResponse<RefreshResponse>> refresh(
      @Valid @RequestBody RefreshRequest request) {
    return SuccessResponse.toOk(authService.reissueToken(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<SuccessResponse<Void>> logout(
      @CurrentMember Long memberId, @Valid @RequestBody LogoutRequest request) {
    authService.logout(memberId, request);
    return SuccessResponse.toOk(null);
  }

  @DeleteMapping("/withdraw")
  public ResponseEntity<SuccessResponse<Void>> withdraw(
      @CurrentMember Long memberId, @Valid @RequestBody WithdrawRequest request) {
    authService.withdraw(memberId, request);
    return SuccessResponse.toOk(null);
  }
}
