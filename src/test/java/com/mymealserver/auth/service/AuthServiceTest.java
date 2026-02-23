package com.mymealserver.auth.service;

import com.mymealserver.api.auth.dto.request.LoginRequest;
import com.mymealserver.api.auth.dto.request.RegisterRequest;
import com.mymealserver.api.auth.dto.request.WithdrawRequest;
import com.mymealserver.api.auth.dto.response.AuthResponse;
import com.mymealserver.api.auth.service.AuthService;
import com.mymealserver.api.auth.service.TokenBlacklistService;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.MemberFixture;
import com.mymealserver.common.test.fixtures.TokenFixture;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.MemberSettings.MemberSettingsWriter;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.MemberSettings.MemberSettings;
import com.mymealserver.domain.MemberWithdrawal.MemberWithdrawal;
import com.mymealserver.domain.MemberWithdrawal.MemberWithdrawalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberWriter memberWriter;

    @Mock
    private MemberSettingsWriter memberSettingsWriter;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberWithdrawalRepository memberWithdrawalRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("회원가입")
    class RegisterTests {

        private Member testMember;

        @BeforeEach
        void setUp() {
            testMember = MemberFixture.createDefaultMember();
        }

        @Test
        @DisplayName("유효한 정보로 회원가입에 성공한다")
        void register_WithValidData_ShouldCreateMember() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("newuser@example.com")
                    .password("Password123!")
                    .name("New User")
                    .fcmToken("test_fcm_token")
                    .build();

            when(memberReader.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
            when(memberWriter.save(any(Member.class))).thenAnswer(invocation -> {
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
            when(memberSettingsWriter.save(any(MemberSettings.class))).thenReturn(MemberSettings.createDefault(testMember));

            // When
            authService.register(request);

            // Then
            verify(memberReader).existsByEmail(request.email());
            verify(passwordEncoder).encode(request.password());
            verify(memberWriter).save(any(Member.class));
            verify(memberSettingsWriter).save(any(MemberSettings.class));
        }

        @Test
        @DisplayName("FCM 토큰 없이 회원가입에 성공한다")
        void register_WithoutFcmToken_ShouldCreateMember() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("newuser@example.com")
                    .password("Password123!")
                    .name("New User")
                    .fcmToken(null)
                    .build();

            when(memberReader.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
            when(memberWriter.save(any(Member.class))).thenAnswer(invocation -> {
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
            when(memberSettingsWriter.save(any(MemberSettings.class))).thenReturn(MemberSettings.createDefault(testMember));

            // When
            authService.register(request);

            // Then
            verify(memberReader).existsByEmail(request.email());
            verify(passwordEncoder).encode(request.password());
            verify(memberWriter).save(any(Member.class));
            verify(memberSettingsWriter).save(any(MemberSettings.class));
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
        void register_WithDuplicateEmail_ShouldThrowException() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@example.com")
                    .password("Password123!")
                    .name("Existing User")
                    .build();

            when(memberReader.existsByEmail(request.email())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS));

            verify(memberReader).existsByEmail(request.email());
            verify(passwordEncoder, never()).encode(anyString());
            verify(memberWriter, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTests {

        private Member testMember;
        private AuthResponse mockAuthResponse;
        private LoginRequest validLoginRequest;

        @BeforeEach
        void setUp() {
            testMember = MemberFixture.createDefaultMember();
            mockAuthResponse = TokenFixture.createAuthResponse(testMember);
            validLoginRequest = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password123!")
                    .fcmToken("test_fcm_token")
                    .build();
        }

        @Test
        @DisplayName("유효한 자격증명으로 로그인에 성공한다")
        void login_WithValidCredentials_ShouldReturnAuthResponse() {
            // Given
            when(memberReader.findByEmail(validLoginRequest.email())).thenReturn(testMember);
            when(passwordEncoder.matches(validLoginRequest.password(), testMember.getPassword())).thenReturn(true);
            when(memberWriter.save(any(Member.class))).thenReturn(testMember);
            when(tokenService.generateTokens(testMember)).thenReturn(mockAuthResponse);

            // When
            AuthResponse response = authService.login(validLoginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(mockAuthResponse.accessToken());
            assertThat(response.refreshToken()).isEqualTo(mockAuthResponse.refreshToken());

            verify(memberReader).findByEmail(validLoginRequest.email());
            verify(passwordEncoder).matches(validLoginRequest.password(), testMember.getPassword());
            verify(memberWriter).save(testMember);
            verify(memberSettingsWriter).updateFcmToken(testMember.getId(), validLoginRequest.fcmToken());
            verify(tokenService).generateTokens(testMember);
        }

        @Test
        @DisplayName("FCM 토큰 없이 로그인에 성공한다")
        void login_WithoutFcmToken_ShouldReturnAuthResponse() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password123!")
                    .fcmToken(null)
                    .build();

            when(memberReader.findByEmail(request.email())).thenReturn(testMember);
            when(passwordEncoder.matches(request.password(), testMember.getPassword())).thenReturn(true);
            when(memberWriter.save(any(Member.class))).thenReturn(testMember);
            when(tokenService.generateTokens(testMember)).thenReturn(mockAuthResponse);

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(mockAuthResponse.accessToken());

            verify(memberReader).findByEmail(request.email());
            verify(passwordEncoder).matches(request.password(), testMember.getPassword());
            verify(memberWriter).save(testMember);
            verify(memberSettingsWriter, never()).updateFcmToken(any(), any());
            verify(tokenService).generateTokens(testMember);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void login_WithNonExistentEmail_ShouldThrowException() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("Password123!")
                    .build();

            when(memberReader.findByEmail(request.email()))
                    .thenThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

            verify(memberReader).findByEmail(request.email());
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        void login_WithWrongPassword_ShouldThrowException() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("WrongPassword123!")
                    .build();

            when(memberReader.findByEmail(request.email())).thenReturn(testMember);
            when(passwordEncoder.matches(request.password(), testMember.getPassword())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));

            verify(memberReader).findByEmail(request.email());
            verify(passwordEncoder).matches(request.password(), testMember.getPassword());
            verify(memberWriter, never()).save(any());
        }

        @Test
        @DisplayName("비활성화된 계정으로 로그인 시 예외가 발생한다")
        void login_WithInactiveAccount_ShouldThrowException() {
            // Given
            Member inactiveMember = MemberFixture.createInactiveMember();
            LoginRequest request = LoginRequest.builder()
                    .email("inactive@example.com")
                    .password("Password123!")
                    .build();

            when(memberReader.findByEmail(request.email())).thenReturn(inactiveMember);
            when(passwordEncoder.matches(request.password(), inactiveMember.getPassword())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(ErrorCode.MEMBER_DEACTIVATED));

            verify(memberReader).findByEmail(request.email());
            verify(passwordEncoder).matches(request.password(), inactiveMember.getPassword());
            verify(memberWriter, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTests {

        @Test
        @DisplayName("리프레시 토큰과 함께 로그아웃에 성공한다")
        void logout_WithRefreshToken_ShouldAddToBlacklist() {
            // Given
            Long memberId = 1L;
            String refreshToken = TokenFixture.VALID_REFRESH_TOKEN;

            // When
            authService.logout(memberId, refreshToken);

            // Then
            verify(tokenBlacklistService).addToBlacklist(refreshToken);
        }

        @Test
        @DisplayName("리프레시 토큰 없이 로그아웃에 성공한다")
        void logout_WithoutRefreshToken_ShouldSucceed() {
            // Given
            Long memberId = 1L;
            String refreshToken = null;

            // When
            authService.logout(memberId, refreshToken);

            // Then
            verify(tokenBlacklistService, never()).addToBlacklist(any());
        }
    }

    @Nested
    @DisplayName("회원탈퇴")
    class WithdrawalTests {

        private Member testMember;

        @BeforeEach
        void setUp() {
            testMember = MemberFixture.createDefaultMember();
        }

        @Test
        @DisplayName("회원 탈퇴에 성공한다")
        void withdraw_WithValidRequest_ShouldCreateWithdrawalRecordAndDeactivateMember() {
            // Given
            Long memberId = 1L;
            WithdrawRequest request = WithdrawRequest.builder()
                    .reason("PRIVACY_CONCERNS")
                    .reasonDetail("Too much data collection")
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWithdrawalRepository.save(any(MemberWithdrawal.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.withdraw(memberId, request);

            // Then
            verify(memberReader).findById(memberId);
            verify(memberWithdrawalRepository).save(any(MemberWithdrawal.class));
            verify(memberWriter).delete(testMember);
            assertThat(testMember.isActive()).isFalse();
        }

        @Test
        @DisplayName("탈퇴 사유만 있고 상세 설명이 없어도 탈퇴에 성공한다")
        void withdraw_WithOnlyReason_ShouldSucceed() {
            // Given
            Long memberId = 1L;
            WithdrawRequest request = WithdrawRequest.builder()
                    .reason("NOT_ENGAGING")
                    .reasonDetail(null)
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWithdrawalRepository.save(any(MemberWithdrawal.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.withdraw(memberId, request);

            // Then
            verify(memberReader).findById(memberId);
            verify(memberWithdrawalRepository).save(any(MemberWithdrawal.class));
            verify(memberWriter).delete(testMember);
        }

        @Test
        @DisplayName("탈퇴 시 회원이 비활성화된다")
        void withdraw_ShouldDeactivateMember() {
            // Given
            Long memberId = 1L;
            WithdrawRequest request = WithdrawRequest.builder()
                    .reason("OTHER")
                    .reasonDetail("Test withdrawal")
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWithdrawalRepository.save(any(MemberWithdrawal.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.withdraw(memberId, request);

            // Then
            assertThat(testMember.isActive()).isFalse();
            verify(memberWriter).delete(testMember);
        }

        @Test
        @DisplayName("탈퇴 시 탈퇴 기록이 생성된다")
        void withdraw_ShouldCreateWithdrawalRecord() {
            // Given
            Long memberId = 1L;
            WithdrawRequest request = WithdrawRequest.builder()
                    .reason("PRIVACY_CONCERNS")
                    .reasonDetail("Test withdrawal")
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWithdrawalRepository.save(any(MemberWithdrawal.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            authService.withdraw(memberId, request);

            // Then
            verify(memberWithdrawalRepository).save(argThat(withdrawal ->
                    withdrawal.getMemberId().equals(memberId) &&
                            withdrawal.getReason().toString().equals("PRIVACY_CONCERNS") &&
                            withdrawal.getReasonDetail().equals("Test withdrawal")
            ));
        }
    }
}
