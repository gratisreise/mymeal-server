package com.mymealserver.api.auth.service;

import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.api.auth.dto.response.MemberResponse;
import com.mymealserver.api.auth.dto.response.RefreshResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.security.JwtProvider;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.redis.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

  private final JwtProvider jwtProvider;
  private final RedisTokenService redisTokenService;

  // 로그인 토큰 생성
  public LoginResponse generateToken(Member member) {
    Long memberId = member.getId();
    String accessToken = jwtProvider.createAccessToken(memberId);
    String refreshToken = jwtProvider.createRefreshToken(memberId);
    redisTokenService.saveRefreshToken(memberId, refreshToken);
    return LoginResponse.of(accessToken, refreshToken, MemberResponse.from(member));
  }

  // 토큰재발급
  public RefreshResponse reissueToken(String refreshToken) {
    // 1. 토큰 검증 & id 추출
    Long memberId = jwtProvider.validateRefreshTokenAndGetMemberId(refreshToken);

    // 2. Redis 대조 (저장된 RT와 일치하는지)
    String storedRT = redisTokenService.getRefreshToken(memberId);

    if (!refreshToken.equals(storedRT)) {
      throw BusinessException.error(ErrorCode.TOKEN_INVALID);
    }

    // 4. 기존 RT 삭제 (블랙리스트 대신)
    redisTokenService.deleteRefreshToken(memberId);

    // 5. 새 토큰 발급
    String newAT = jwtProvider.createAccessToken(memberId);
    String newRT = jwtProvider.createRefreshToken(memberId);
    redisTokenService.saveRefreshToken(memberId, newRT);

    return RefreshResponse.of(newAT, newRT);
  }

  //    public static String extractToken(String token) {
  //        return token.replace(CommonValue.AUTH_PREFIX, "");
  //    }
}
