package api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.api.auth.dto.response.MemberResponse;
import com.mymealserver.api.auth.dto.response.RefreshResponse;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.security.JwtProvider;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.redis.RedisTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock private JwtProvider jwtProvider;
  @Mock private RedisTokenService redisTokenService;

  @InjectMocks private TokenService tokenService;

  private Member createActiveMember() {
    return Member.builder()
        .id(1L)
        .email("test@example.com")
        .password("encodedPassword")
        .name("tester")
        .provider(ProviderType.EMAIL)
        .isActive(true)
        .build();
  }

  @Test
  void generateToken_success() {
    // given
    Member member = createActiveMember();
    given(jwtProvider.createAccessToken(1L)).willReturn("access-token");
    given(jwtProvider.createRefreshToken(1L)).willReturn("refresh-token");

    // when
    LoginResponse response = tokenService.generateToken(member);

    // then
    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.member().id()).isEqualTo(1L);
    assertThat(response.member().email()).isEqualTo("test@example.com");
    assertThat(response.member().name()).isEqualTo("tester");
    assertThat(response.member().provider()).isEqualTo(ProviderType.EMAIL);
    assertThat(response.member().isActive()).isTrue();

    verify(redisTokenService).saveRefreshToken(1L, "refresh-token");
  }

  @Test
  void reissueToken_success() {
    // given
    given(jwtProvider.validateRefreshTokenAndGetMemberId("valid-rt")).willReturn(1L);
    given(redisTokenService.getRefreshToken(1L)).willReturn("valid-rt");
    given(jwtProvider.createAccessToken(1L)).willReturn("new-at");
    given(jwtProvider.createRefreshToken(1L)).willReturn("new-rt");

    // when
    RefreshResponse response = tokenService.reissueToken("valid-rt");

    // then
    assertThat(response.accessToken()).isEqualTo("new-at");
    assertThat(response.refreshToken()).isEqualTo("new-rt");

    InOrder inOrder = inOrder(redisTokenService);
    inOrder.verify(redisTokenService).deleteRefreshToken(1L);
    inOrder.verify(redisTokenService).saveRefreshToken(1L, "new-rt");
  }

  @Test
  void reissueToken_throwsException_whenJwtValidationFails() {
    // given
    given(jwtProvider.validateRefreshTokenAndGetMemberId("invalid-rt"))
        .willThrow(BusinessException.error(ErrorCode.REFRESH_TOKEN_INVALID));

    // when & then
    assertThatThrownBy(() -> tokenService.reissueToken("invalid-rt"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);

    verify(redisTokenService, never()).getRefreshToken(1L);
  }

  @Test
  void reissueToken_throwsException_whenStoredTokenMismatch() {
    // given
    given(jwtProvider.validateRefreshTokenAndGetMemberId("client-rt")).willReturn(1L);
    given(redisTokenService.getRefreshToken(1L)).willReturn("different-rt");

    // when & then
    assertThatThrownBy(() -> tokenService.reissueToken("client-rt"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.TOKEN_INVALID);

    verify(redisTokenService, never()).deleteRefreshToken(1L);
    verify(jwtProvider, never()).createAccessToken(1L);
  }

  @Test
  void reissueToken_throwsException_whenStoredTokenNull() {
    // given
    given(jwtProvider.validateRefreshTokenAndGetMemberId("valid-rt")).willReturn(1L);
    given(redisTokenService.getRefreshToken(1L)).willReturn(null);

    // when & then
    assertThatThrownBy(() -> tokenService.reissueToken("valid-rt"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.TOKEN_INVALID);

    verify(jwtProvider, never()).createAccessToken(1L);
  }

  @Test
  void invalidateTokens_success() {
    // given
    given(jwtProvider.getExpiration("access-token")).willReturn(3600000L);

    // when
    tokenService.invalidateTokens(1L, "access-token");

    // then
    verify(redisTokenService).addBlacklist("access-token", 3600000L);
    verify(redisTokenService).deleteRefreshToken(1L);
  }
}
