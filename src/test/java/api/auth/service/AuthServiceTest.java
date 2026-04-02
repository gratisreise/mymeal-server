package api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.mymealserver.api.auth.dto.request.LoginRequest;
import com.mymealserver.api.auth.dto.request.LogoutRequest;
import com.mymealserver.api.auth.dto.request.RefreshRequest;
import com.mymealserver.api.auth.dto.request.RegisterRequest;
import com.mymealserver.api.auth.dto.request.WithdrawRequest;
import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.api.auth.dto.response.MemberResponse;
import com.mymealserver.api.auth.dto.response.RefreshResponse;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.enums.WithdrawalReason;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsWriter;
import com.mymealserver.domain.memberwithdrawal.MemberWithdrawal;
import com.mymealserver.domain.memberwithdrawal.MemberWithdrawalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private MemberReader memberReader;
  @Mock private MemberWriter memberWriter;
  @Mock private MemberSettingsWriter memberSettingsWriter;
  @Mock private TokenService tokenService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private MemberWithdrawalRepository memberWithdrawalRepository;

  @InjectMocks private com.mymealserver.api.auth.service.AuthService authService;

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

  // ===== register =====

  @Test
  void register_success_withFcmToken() {
    // given
    RegisterRequest request =
        RegisterRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .name("tester")
            .fcmToken("fcm-token-123")
            .build();

    given(memberReader.existsByEmail("test@example.com")).willReturn(false);
    given(passwordEncoder.encode("Password123!")).willReturn("encodedPw");
    given(memberWriter.save(any(Member.class)))
        .willReturn(
            Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPw")
                .name("tester")
                .provider(ProviderType.EMAIL)
                .isActive(true)
                .build());
    given(memberSettingsWriter.save(any(MemberSettings.class)))
        .willAnswer(inv -> inv.getArgument(0));

    // when
    authService.register(request);

    // then
    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    verify(memberWriter).save(memberCaptor.capture());
    assertThat(memberCaptor.getValue().getEmail()).isEqualTo("test@example.com");
    assertThat(memberCaptor.getValue().getPassword()).isEqualTo("encodedPw");
    assertThat(memberCaptor.getValue().getProvider()).isEqualTo(ProviderType.EMAIL);

    ArgumentCaptor<MemberSettings> settingsCaptor = ArgumentCaptor.forClass(MemberSettings.class);
    verify(memberSettingsWriter).save(settingsCaptor.capture());
    assertThat(settingsCaptor.getValue().getFcmToken()).isEqualTo("fcm-token-123");
    assertThat(settingsCaptor.getValue().getMemberId()).isEqualTo(1L);
  }

  @Test
  void register_success_withoutFcmToken() {
    // given
    RegisterRequest request =
        RegisterRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .name("tester")
            .build();

    given(memberReader.existsByEmail("test@example.com")).willReturn(false);
    given(passwordEncoder.encode("Password123!")).willReturn("encodedPw");
    given(memberWriter.save(any(Member.class)))
        .willReturn(
            Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPw")
                .name("tester")
                .provider(ProviderType.EMAIL)
                .isActive(true)
                .build());
    given(memberSettingsWriter.save(any(MemberSettings.class)))
        .willAnswer(inv -> inv.getArgument(0));

    // when
    authService.register(request);

    // then
    ArgumentCaptor<MemberSettings> settingsCaptor = ArgumentCaptor.forClass(MemberSettings.class);
    verify(memberSettingsWriter).save(settingsCaptor.capture());
    assertThat(settingsCaptor.getValue().getFcmToken()).isNull();
  }

  @Test
  void register_throwsException_whenEmailAlreadyExists() {
    // given
    RegisterRequest request =
        RegisterRequest.builder()
            .email("duplicate@example.com")
            .password("Password123!")
            .name("tester")
            .build();

    given(memberReader.existsByEmail("duplicate@example.com")).willReturn(true);

    // when & then
    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);

    verify(passwordEncoder, never()).encode(any());
    verify(memberWriter, never()).save(any(Member.class));
    verify(memberSettingsWriter, never()).save(any(MemberSettings.class));
  }

  // ===== login =====

  @Test
  void login_success_withFcmToken() {
    // given
    Member member = createActiveMember();
    LoginRequest request =
        LoginRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .fcmToken("fcm-token")
            .build();

    LoginResponse expectedResponse =
        LoginResponse.of("access-token", "refresh-token", MemberResponse.from(member));

    given(memberReader.findByEmail("test@example.com")).willReturn(member);
    given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);
    given(memberWriter.save(any(Member.class))).willReturn(member);
    given(tokenService.generateToken(member)).willReturn(expectedResponse);

    // when
    LoginResponse response = authService.login(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(memberWriter).save(any(Member.class));
    verify(memberSettingsWriter).updateFcmToken(1L, "fcm-token");
  }

  @Test
  void login_success_withoutFcmToken() {
    // given
    Member member = createActiveMember();
    LoginRequest request =
        LoginRequest.builder().email("test@example.com").password("Password123!").build();

    LoginResponse expectedResponse =
        LoginResponse.of("access-token", "refresh-token", MemberResponse.from(member));

    given(memberReader.findByEmail("test@example.com")).willReturn(member);
    given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);
    given(memberWriter.save(any(Member.class))).willReturn(member);
    given(tokenService.generateToken(member)).willReturn(expectedResponse);

    // when
    LoginResponse response = authService.login(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(memberSettingsWriter, never()).updateFcmToken(anyLong(), any());
  }

  @Test
  void login_throwsException_whenMemberNotFound() {
    // given
    LoginRequest request =
        LoginRequest.builder().email("unknown@example.com").password("Password123!").build();

    given(memberReader.findByEmail("unknown@example.com"))
        .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

    verify(passwordEncoder, never()).matches(any(), any());
    verify(tokenService, never()).generateToken(any(Member.class));
  }

  @Test
  void login_throwsException_whenPasswordMismatch() {
    // given
    Member member = createActiveMember();
    LoginRequest request =
        LoginRequest.builder().email("test@example.com").password("wrongPassword").build();

    given(memberReader.findByEmail("test@example.com")).willReturn(member);
    given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

    // when & then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

    verify(memberWriter, never()).save(any(Member.class));
    verify(tokenService, never()).generateToken(any(Member.class));
  }

  @Test
  void login_throwsException_whenMemberDeactivated() {
    // given
    Member deactivatedMember =
        Member.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .name("tester")
            .provider(ProviderType.EMAIL)
            .isActive(false)
            .build();

    LoginRequest request =
        LoginRequest.builder().email("test@example.com").password("Password123!").build();

    given(memberReader.findByEmail("test@example.com")).willReturn(deactivatedMember);
    given(passwordEncoder.matches("Password123!", "encodedPassword")).willReturn(true);

    // when & then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEMBER_DEACTIVATED);

    verify(memberWriter, never()).save(any(Member.class));
    verify(tokenService, never()).generateToken(any(Member.class));
  }

  // ===== reissueToken =====

  @Test
  void reissueToken_success() {
    // given
    RefreshRequest request = RefreshRequest.builder().refreshToken("valid-rt").build();
    RefreshResponse expected = RefreshResponse.of("new-at", "new-rt");

    given(tokenService.reissueToken("valid-rt")).willReturn(expected);

    // when
    RefreshResponse response = authService.reissueToken(request);

    // then
    assertThat(response.accessToken()).isEqualTo("new-at");
    assertThat(response.refreshToken()).isEqualTo("new-rt");
  }

  @Test
  void reissueToken_propagatesException_whenTokenInvalid() {
    // given
    RefreshRequest request = RefreshRequest.builder().refreshToken("bad-rt").build();

    given(tokenService.reissueToken("bad-rt"))
        .willThrow(BusinessException.error(ErrorCode.TOKEN_INVALID));

    // when & then
    assertThatThrownBy(() -> authService.reissueToken(request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.TOKEN_INVALID);
  }

  // ===== logout =====

  @Test
  void logout_success() {
    // given
    LogoutRequest request = LogoutRequest.builder().accessToken("access-token").build();

    // when
    authService.logout(1L, request);

    // then
    verify(tokenService).invalidateTokens(1L, "access-token");
  }

  // ===== withdraw =====

  @Test
  void withdraw_success() {
    // given
    Member member = createActiveMember();
    WithdrawRequest request =
        WithdrawRequest.builder()
            .reason("PRIVACY_CONCERNS")
            .reasonDetail("worried about data")
            .build();

    given(memberReader.findById(1L)).willReturn(member);
    given(memberWithdrawalRepository.save(any(MemberWithdrawal.class)))
        .willAnswer(inv -> inv.getArgument(0));

    // when
    authService.withdraw(1L, request);

    // then
    ArgumentCaptor<MemberWithdrawal> captor = ArgumentCaptor.forClass(MemberWithdrawal.class);
    verify(memberWithdrawalRepository).save(captor.capture());

    MemberWithdrawal withdrawal = captor.getValue();
    assertThat(withdrawal.getMemberId()).isEqualTo(1L);
    assertThat(withdrawal.getReason()).isEqualTo(WithdrawalReason.PRIVACY_CONCERNS);
    assertThat(withdrawal.getReasonDetail()).isEqualTo("worried about data");

    verify(memberWriter).delete(member);
    assertThat(member.isActive()).isFalse();
  }

  @Test
  void withdraw_throwsException_whenMemberNotFound() {
    // given
    WithdrawRequest request = WithdrawRequest.builder().reason("OTHER").build();

    given(memberReader.findById(999L)).willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> authService.withdraw(999L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

    verify(memberWithdrawalRepository, never()).save(any(MemberWithdrawal.class));
    verify(memberWriter, never()).delete(any(Member.class));
  }
}
