package com.mymealserver.auth.service;

import com.mymealserver.api.auth.service.TokenBlacklistService;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.MemberFixture;
import com.mymealserver.common.test.fixtures.TokenFixture;
import com.mymealserver.common.security.JwtTokenProvider;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService 단위 테스트")
class TokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberReader memberReader;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private TokenService tokenService;

    @Nested
    @DisplayName("토큰 생성")
    class TokenGenerationTests {

        private Member testMember;

        @BeforeEach
        void setUp() {
            testMember = MemberFixture.createDefaultMember();
        }

        @Test
        @DisplayName("유효한 회원에 대해 액세스 토큰과 리프레시 토큰을 생성한다")
        void generateTokens_WithValidMember_ShouldReturnAuthResponse() {
            // Given
            Long memberId = 1L;
            String expectedAccessToken = "new_access_token";
            String expectedRefreshToken = "new_refresh_token";

            when(jwtTokenProvider.createAccessToken(memberId)).thenReturn(expectedAccessToken);
            when(jwtTokenProvider.createRefreshToken(memberId)).thenReturn(expectedRefreshToken);

            // When
            var response = tokenService.generateTokens(testMember);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(expectedAccessToken);
            assertThat(response.refreshToken()).isEqualTo(expectedRefreshToken);
            assertThat(response.member()).isNotNull();
            assertThat(response.member().id()).isEqualTo(memberId);

            verify(jwtTokenProvider).createAccessToken(memberId);
            verify(jwtTokenProvider).createRefreshToken(memberId);
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class TokenRefreshTests {

        private Member testMember;

        @BeforeEach
        void setUp() {
            testMember = MemberFixture.createDefaultMember();
        }

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새로운 토큰을 발급한다")
        void refreshToken_WithValidToken_ShouldReturnNewTokens() {
            // Given
            String refreshToken = TokenFixture.VALID_REFRESH_TOKEN;
            Long memberId = 1L;
            String newAccessToken = "new_access_token";
            String newRefreshToken = "new_refresh_token";

            when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(false);
            when(jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken)).thenReturn(memberId);
            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(jwtTokenProvider.createAccessToken(memberId)).thenReturn(newAccessToken);
            when(jwtTokenProvider.createRefreshToken(memberId)).thenReturn(newRefreshToken);

            // When
            var response = tokenService.refreshToken(refreshToken);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(newAccessToken);
            assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

            verify(tokenBlacklistService).isBlacklisted(refreshToken);
            verify(jwtTokenProvider).validateRefreshTokenAndGetMemberId(refreshToken);
            verify(memberReader).findById(memberId);
            verify(jwtTokenProvider).createAccessToken(memberId);
            verify(jwtTokenProvider).createRefreshToken(memberId);
        }

        @Test
        @DisplayName("블랙리스트에 있는 리프레시 토큰으로 갱신하면 예외가 발생한다")
        void refreshToken_WithBlacklistedToken_ShouldThrowException() {
            // Given
            String blacklistedToken = TokenFixture.BLACKLISTED_REFRESH_TOKEN;

            when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tokenService.refreshToken(blacklistedToken))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID));

            verify(tokenBlacklistService).isBlacklisted(blacklistedToken);
            verify(jwtTokenProvider, never()).validateRefreshTokenAndGetMemberId(anyString());
            verify(memberReader, never()).findById(any());
        }

        @Test
        @DisplayName("비활성화된 회원의 리프레시 토큰으로 갱신하면 예외가 발생한다")
        void refreshToken_WithInactiveMember_ShouldThrowException() {
            // Given
            String refreshToken = TokenFixture.VALID_REFRESH_TOKEN;
            Long memberId = 5L;
            Member inactiveMember = MemberFixture.createInactiveMember();

            when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(false);
            when(jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken)).thenReturn(memberId);
            when(memberReader.findById(memberId)).thenReturn(inactiveMember);

            // When & Then
            assertThatThrownBy(() -> tokenService.refreshToken(refreshToken))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.MEMBER_DEACTIVATED));

            verify(tokenBlacklistService).isBlacklisted(refreshToken);
            verify(jwtTokenProvider).validateRefreshTokenAndGetMemberId(refreshToken);
            verify(memberReader).findById(memberId);
            verify(jwtTokenProvider, never()).createAccessToken(any());
        }

        @Test
        @DisplayName("리프레시 토큰 갱신 시 블랙리스트 확인 후 회원 조회를 수행한다")
        void refreshToken_OrderOfOperations_ShouldCheckBlacklistFirst() {
            // Given
            String refreshToken = TokenFixture.VALID_REFRESH_TOKEN;
            Long memberId = 1L;

            when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(false);
            when(jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken)).thenReturn(memberId);
            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(jwtTokenProvider.createAccessToken(memberId)).thenReturn("new_access");
            when(jwtTokenProvider.createRefreshToken(memberId)).thenReturn("new_refresh");

            // When
            tokenService.refreshToken(refreshToken);

            // Then - Verify the order of operations
            var inOrder = org.mockito.Mockito.inOrder(
                    tokenBlacklistService,
                    jwtTokenProvider,
                    memberReader
            );

            inOrder.verify(tokenBlacklistService).isBlacklisted(refreshToken);
            inOrder.verify(jwtTokenProvider).validateRefreshTokenAndGetMemberId(refreshToken);
            inOrder.verify(memberReader).findById(memberId);
            inOrder.verify(jwtTokenProvider).createAccessToken(memberId);
            inOrder.verify(jwtTokenProvider).createRefreshToken(memberId);
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class TokenValidationTests {

        @Test
        @DisplayName("액세스 토큰을 검증하고 회원 ID를 반환한다")
        void validateAccessToken_WithValidToken_ShouldReturnMemberId() {
            // Given
            String accessToken = TokenFixture.VALID_ACCESS_TOKEN;
            Long memberId = 1L;

            when(jwtTokenProvider.validateAccessTokenAndGetMemberId(accessToken)).thenReturn(memberId);

            // When
            Long result = tokenService.validateAccessToken(accessToken);

            // Then
            assertThat(result).isEqualTo(memberId);
            verify(jwtTokenProvider).validateAccessTokenAndGetMemberId(accessToken);
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰 검증 시 예외가 발생한다")
        void validateAccessToken_WithInvalidToken_ShouldThrowException() {
            // Given
            String invalidToken = TokenFixture.INVALID_TOKEN;

            when(jwtTokenProvider.validateAccessTokenAndGetMemberId(invalidToken))
                    .thenThrow(new BusinessException(ErrorCode.TOKEN_INVALID));

            // When & Then
            assertThatThrownBy(() -> tokenService.validateAccessToken(invalidToken))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.TOKEN_INVALID));

            verify(jwtTokenProvider).validateAccessTokenAndGetMemberId(invalidToken);
        }

        @Test
        @DisplayName("리프레시 토큰을 검증하고 회원 ID를 반환한다")
        void validateRefreshToken_WithValidToken_ShouldReturnMemberId() {
            // Given
            String refreshToken = TokenFixture.VALID_REFRESH_TOKEN;
            Long memberId = 1L;

            when(jwtTokenProvider.validateRefreshTokenAndGetMemberId(refreshToken)).thenReturn(memberId);

            // When
            Long result = tokenService.validateRefreshToken(refreshToken);

            // Then
            assertThat(result).isEqualTo(memberId);
            verify(jwtTokenProvider).validateRefreshTokenAndGetMemberId(refreshToken);
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰 검증 시 예외가 발생한다")
        void validateRefreshToken_WithInvalidToken_ShouldThrowException() {
            // Given
            String invalidToken = TokenFixture.INVALID_TOKEN;

            when(jwtTokenProvider.validateRefreshTokenAndGetMemberId(invalidToken))
                    .thenThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

            // When & Then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(invalidToken))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID));

            verify(jwtTokenProvider).validateRefreshTokenAndGetMemberId(invalidToken);
        }
    }
}
