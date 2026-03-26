package com.mymealserver.api.auth.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.response.AuthResponse;
import com.mymealserver.api.auth.dto.response.MemberResponse;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.api.auth.service.oauth.impl.KakaoOAuthService;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.test.fixtures.OAuthFixture;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsWriter;
import com.mymealserver.external.oauth.kakao.KakaoApiClient;
import com.mymealserver.external.oauth.kakao.KakaoTokenResponse;
import com.mymealserver.external.oauth.kakao.KakaoUserInfoResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoOAuthService 단위 테스트")
class KakaoOAuthServiceTest {

    @Mock
    private KakaoApiClient apiClient;

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberWriter memberWriter;

    @Mock
    private MemberSettingsWriter memberSettingsWriter;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private KakaoOAuthService kakaoOAuthService;

    @Nested
    @DisplayName("신규 사용자 인증")
    class NewUserAuthenticationTests {

        private OAuthRequest validRequest;
        private KakaoTokenResponse tokenResponse;
        private KakaoUserInfoResponse userInfoResponse;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createKakaoTokenResponse();
            userInfoResponse = OAuthFixture.createKakaoUserInfo();
            mockAuthResponse = AuthResponse.builder()
                    .accessToken("test_access_token")
                    .refreshToken("test_refresh_token")
                    .build();
        }

        @Test
        @DisplayName("신규 사용자 OAuth 인증에 성공한다")
        void authenticate_WithNewUser_ShouldCreateMemberAndReturnTokens() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()))).willReturn(null);
            given(memberWriter.save(any(Member.class))).willAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                return Member.builder()
                        .id(1L)
                        .email(member.getEmail())
                        .name(member.getName())
                        .profileImage(member.getProfileImage())
                        .provider(member.getProvider())
                        .providerId(member.getProviderId())
                        .isActive(member.getIsActive())
                        .lastLoginAt(member.getLastLoginAt())
                        .build();
            });
            given(memberSettingsWriter.createDefault(any(Member.class))).willReturn(mock(MemberSettings.class));
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(validRequest);

            // Then
            then(apiClient).should().exchangeCodeForToken(validRequest.code());
            then(apiClient).should().getUserInfo(tokenResponse.accessToken());
            then(memberReader).should().findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()));
            then(memberWriter).should(times(2)).save(any(Member.class));
            then(memberSettingsWriter).should().createDefault(any(Member.class));
            then(tokenService).should().generateTokens(any(Member.class));
        }

        @Test
        @DisplayName("이름이 null인 경우 기본값 'User'로 설정된다")
        void authenticate_WithNullName_ShouldUseDefaultName() {
            // Given
            KakaoUserInfoResponse userInfoWithNullName = OAuthFixture.createKakaoUserInfoWithNullNickname();

            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoWithNullName);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoWithNullName.id()))).willReturn(null);
            given(memberWriter.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(memberSettingsWriter.createDefault(any(Member.class))).willReturn(mock(MemberSettings.class));
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(validRequest);

            // Then
            then(memberWriter).should(times(2)).save(any(Member.class));
        }

        @Test
        @DisplayName("신규 회원 생성 시 이메일 형식은 providerId@kakao.com이다")
        void authenticate_WithNewUser_ShouldCreateEmailWithCorrectFormat() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()))).willReturn(null);
            given(memberWriter.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(memberSettingsWriter.createDefault(any(Member.class))).willReturn(mock(MemberSettings.class));
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(validRequest);

            // Then
            then(memberWriter).should(times(2)).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("기존 사용자 인증")
    class ExistingUserAuthenticationTests {

        private OAuthRequest validRequest;
        private KakaoTokenResponse tokenResponse;
        private KakaoUserInfoResponse userInfoResponse;
        private Member testMember;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createKakaoTokenResponse();
            userInfoResponse = OAuthFixture.createKakaoUserInfo();
            testMember = Member.builder()
                    .id(1L)
                    .email(OAuthFixture.KAKAO_USER_ID + "@kakao.com")
                    .name(OAuthFixture.KAKAO_USER_NAME)
                    .profileImage(OAuthFixture.KAKAO_PROFILE_IMAGE)
                    .provider(ProviderType.KAKAO)
                    .providerId(OAuthFixture.KAKAO_USER_ID)
                    .isActive(true)
                    .build();
            mockAuthResponse = AuthResponse.builder()
                    .accessToken("test_access_token")
                    .refreshToken("test_refresh_token")
                    .member(MemberResponse.from(testMember))
                    .build();
        }

        @Test
        @DisplayName("기존 사용자 OAuth 인증에 성공한다")
        void authenticate_WithExistingUser_ShouldUpdateLastLoginAndReturnTokens() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()))).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(validRequest);

            // Then
            then(apiClient).should().exchangeCodeForToken(validRequest.code());
            then(apiClient).should().getUserInfo(tokenResponse.accessToken());
            then(memberReader).should().findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()));
            then(memberWriter).should().save(testMember);
            then(memberSettingsWriter).should(never()).createDefault(any());
            then(tokenService).should().generateTokens(testMember);
        }
    }

    @Nested
    @DisplayName("인증 실패")
    class AuthenticationFailureTests {

        private OAuthRequest validRequest;
        private KakaoTokenResponse tokenResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();
            tokenResponse = OAuthFixture.createKakaoTokenResponse();
        }

        @Test
        @DisplayName("잘못된 인증 코드로 토큰 교환 시 예외가 발생한다")
        void authenticate_WithInvalidCode_ShouldThrowException() {
            // Given
            OAuthRequest invalidRequest = OAuthRequest.builder()
                    .code(OAuthFixture.INVALID_AUTH_CODE)
                    .build();

            given(apiClient.exchangeCodeForToken(invalidRequest.code()))
                    .willThrow(new org.springframework.web.client.RestClientException("Invalid authorization code"));

            // When & Then
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> kakaoOAuthService.authenticate(invalidRequest))
                    .isInstanceOf(org.springframework.web.client.RestClientException.class);

            then(apiClient).should().exchangeCodeForToken(invalidRequest.code());
            then(apiClient).should(never()).getUserInfo(anyString());
        }

        @Test
        @DisplayName("사용자 정보 조회 실패 시 예외가 발생한다")
        void authenticate_WhenUserInfoFails_ShouldThrowException() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken()))
                    .willThrow(new org.springframework.web.client.RestClientException("Failed to fetch user info"));

            // When & Then
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> kakaoOAuthService.authenticate(validRequest))
                    .isInstanceOf(org.springframework.web.client.RestClientException.class);

            then(apiClient).should().exchangeCodeForToken(validRequest.code());
            then(apiClient).should().getUserInfo(tokenResponse.accessToken());
            then(memberReader).should(never()).findByProviderAndProviderId(any(), anyString());
        }
    }

    @Nested
    @DisplayName("FCM 토큰 처리")
    class FcmTokenTests {

        private OAuthRequest validRequest;
        private KakaoTokenResponse tokenResponse;
        private KakaoUserInfoResponse userInfoResponse;
        private Member testMember;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createKakaoTokenResponse();
            userInfoResponse = OAuthFixture.createKakaoUserInfo();
            testMember = Member.builder()
                    .id(1L)
                    .email(OAuthFixture.KAKAO_USER_ID + "@kakao.com")
                    .name(OAuthFixture.KAKAO_USER_NAME)
                    .profileImage(OAuthFixture.KAKAO_PROFILE_IMAGE)
                    .provider(ProviderType.KAKAO)
                    .providerId(OAuthFixture.KAKAO_USER_ID)
                    .isActive(true)
                    .build();
            mockAuthResponse = AuthResponse.builder()
                    .accessToken("test_access_token")
                    .refreshToken("test_refresh_token")
                    .member(MemberResponse.from(testMember))
                    .build();
        }

        @Test
        @DisplayName("FCM 토큰이 있는 경우 FCM 토큰을 업데이트한다")
        void authenticate_WithFcmToken_ShouldUpdateFcmToken() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()))).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(validRequest);

            // Then
            then(memberSettingsWriter).should().updateFcmToken(testMember.getId(), validRequest.fcmToken());
        }

        @Test
        @DisplayName("FCM 토큰이 없는 경우 FCM 토큰을 업데이트하지 않는다")
        void authenticate_WithoutFcmToken_ShouldNotUpdateFcmToken() {
            // Given
            OAuthRequest requestWithoutFcm = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken(null)
                    .build();

            given(apiClient.exchangeCodeForToken(requestWithoutFcm.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.KAKAO), eq(userInfoResponse.id()))).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            kakaoOAuthService.authenticate(requestWithoutFcm);

            // Then
            then(memberSettingsWriter).should(never()).updateFcmToken(any(), any());
        }
    }

    @Nested
    @DisplayName("제공자 확인")
    class ProviderTests {

        @Test
        @DisplayName("getProvider()는 KAKAO를 반환한다")
        void getProvider_ShouldReturnKakao() {
            // When
            ProviderType provider = kakaoOAuthService.getProvider();

            // Then
            Assertions.assertThat(provider).isEqualTo(ProviderType.KAKAO);
        }
    }
}
