package com.mymealserver.auth.service.impl;

import com.mymealserver.auth.dto.request.OAuthRequest;
import com.mymealserver.auth.dto.response.AuthResponse;
import com.mymealserver.auth.service.TokenService;
import com.mymealserver.auth.service.client.google.GoogleApiClient;
import com.mymealserver.auth.service.client.google.GoogleTokenResponse;
import com.mymealserver.auth.service.client.google.GoogleUserInfoResponse;
import com.mymealserver.common.test.fixtures.OAuthFixture;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberSettingsWriter;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.enums.ProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleOAuthService 단위 테스트")
class GoogleOAuthServiceTest {

    @Mock
    private GoogleApiClient apiClient;

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberWriter memberWriter;

    @Mock
    private MemberSettingsWriter memberSettingsWriter;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    @Nested
    @DisplayName("신규 사용자 인증")
    class NewUserAuthenticationTests {

        private OAuthRequest validRequest;
        private GoogleTokenResponse tokenResponse;
        private GoogleUserInfoResponse userInfoResponse;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createGoogleTokenResponse();
            userInfoResponse = OAuthFixture.createGoogleUserInfo();
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
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()))).willReturn(null);
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
            googleOAuthService.authenticate(validRequest);

            // Then
            then(apiClient).should().exchangeCodeForToken(validRequest.code());
            then(apiClient).should().getUserInfo(tokenResponse.accessToken());
            then(memberReader).should().findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()));
            then(memberWriter).should(times(2)).save(any(Member.class));
            then(memberSettingsWriter).should().createDefault(any(Member.class));
            then(tokenService).should().generateTokens(any(Member.class));
        }

        @Test
        @DisplayName("이름이 null인 경우 기본값 'User'로 설정된다")
        void authenticate_WithNullName_ShouldUseDefaultName() {
            // Given
            GoogleUserInfoResponse userInfoWithNullName = OAuthFixture.createGoogleUserInfoWithNullName();

            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoWithNullName);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoWithNullName.id()))).willReturn(null);
            given(memberWriter.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(memberSettingsWriter.createDefault(any(Member.class))).willReturn(mock(MemberSettings.class));
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            googleOAuthService.authenticate(validRequest);

            // Then
            then(memberWriter).should(times(2)).save(any(Member.class));
        }

        @Test
        @DisplayName("신규 회원 생성 시 이메일 형식은 providerId@google.com이다")
        void authenticate_WithNewUser_ShouldCreateEmailWithCorrectFormat() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()))).willReturn(null);
            given(memberWriter.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(memberSettingsWriter.createDefault(any(Member.class))).willReturn(mock(MemberSettings.class));
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            googleOAuthService.authenticate(validRequest);

            // Then
            then(memberWriter).should(times(2)).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("기존 사용자 인증")
    class ExistingUserAuthenticationTests {

        private OAuthRequest validRequest;
        private GoogleTokenResponse tokenResponse;
        private GoogleUserInfoResponse userInfoResponse;
        private Member testMember;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createGoogleTokenResponse();
            userInfoResponse = OAuthFixture.createGoogleUserInfo();
            testMember = Member.builder()
                    .id(1L)
                    .email(OAuthFixture.GOOGLE_USER_ID + "@google.com")
                    .name(OAuthFixture.GOOGLE_USER_NAME)
                    .profileImage(OAuthFixture.GOOGLE_PROFILE_IMAGE)
                    .provider(ProviderType.GOOGLE)
                    .providerId(OAuthFixture.GOOGLE_USER_ID)
                    .isActive(true)
                    .build();
            mockAuthResponse = AuthResponse.builder()
                    .accessToken("test_access_token")
                    .refreshToken("test_refresh_token")
                    .member(com.mymealserver.auth.dto.response.MemberResponse.from(testMember))
                    .build();
        }

        @Test
        @DisplayName("기존 사용자 OAuth 인증에 성공한다")
        void authenticate_WithExistingUser_ShouldUpdateLastLoginAndReturnTokens() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()))).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            googleOAuthService.authenticate(validRequest);

            // Then
            then(apiClient).should().exchangeCodeForToken(validRequest.code());
            then(apiClient).should().getUserInfo(tokenResponse.accessToken());
            then(memberReader).should().findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()));
            then(memberWriter).should().save(testMember);
            then(memberSettingsWriter).should(never()).createDefault(any());
            then(tokenService).should().generateTokens(testMember);
        }
    }

    @Nested
    @DisplayName("인증 실패")
    class AuthenticationFailureTests {

        private OAuthRequest validRequest;
        private GoogleTokenResponse tokenResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();
            tokenResponse = OAuthFixture.createGoogleTokenResponse();
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
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> googleOAuthService.authenticate(invalidRequest))
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
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> googleOAuthService.authenticate(validRequest))
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
        private GoogleTokenResponse tokenResponse;
        private GoogleUserInfoResponse userInfoResponse;
        private Member testMember;
        private AuthResponse mockAuthResponse;

        @BeforeEach
        void setUp() {
            validRequest = OAuthRequest.builder()
                    .code(OAuthFixture.VALID_AUTH_CODE)
                    .fcmToken("test_fcm_token")
                    .build();

            tokenResponse = OAuthFixture.createGoogleTokenResponse();
            userInfoResponse = OAuthFixture.createGoogleUserInfo();
            testMember = Member.builder()
                    .id(1L)
                    .email(OAuthFixture.GOOGLE_USER_ID + "@google.com")
                    .name(OAuthFixture.GOOGLE_USER_NAME)
                    .profileImage(OAuthFixture.GOOGLE_PROFILE_IMAGE)
                    .provider(ProviderType.GOOGLE)
                    .providerId(OAuthFixture.GOOGLE_USER_ID)
                    .isActive(true)
                    .build();
            mockAuthResponse = AuthResponse.builder()
                    .accessToken("test_access_token")
                    .refreshToken("test_refresh_token")
                    .member(com.mymealserver.auth.dto.response.MemberResponse.from(testMember))
                    .build();
        }

        @Test
        @DisplayName("FCM 토큰이 있는 경우 FCM 토큰을 업데이트한다")
        void authenticate_WithFcmToken_ShouldUpdateFcmToken() {
            // Given
            given(apiClient.exchangeCodeForToken(validRequest.code())).willReturn(tokenResponse);
            given(apiClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfoResponse);
            given(memberReader.findByProviderAndProviderId(eq(ProviderType.GOOGLE), eq(userInfoResponse.id()))).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(mockAuthResponse);

            // When
            googleOAuthService.authenticate(validRequest);

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
            given(memberReader.findByProviderAndProviderId(ProviderType.GOOGLE, userInfoResponse.id())).willReturn(testMember);
            given(memberWriter.save(any(Member.class))).willReturn(testMember);
            given(tokenService.generateTokens(any(Member.class))).willReturn(any());

            // When
            googleOAuthService.authenticate(requestWithoutFcm);

            // Then
            then(memberSettingsWriter).should(never()).updateFcmToken(any(), any());
        }
    }

    @Nested
    @DisplayName("제공자 확인")
    class ProviderTests {

        @Test
        @DisplayName("getProvider()는 GOOGLE을 반환한다")
        void getProvider_ShouldReturnGoogle() {
            // When
            ProviderType provider = googleOAuthService.getProvider();

            // Then
            assertThat(provider).isEqualTo(ProviderType.GOOGLE);
        }
    }
}
