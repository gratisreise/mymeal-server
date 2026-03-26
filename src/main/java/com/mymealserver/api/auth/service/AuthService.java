package com.mymealserver.api.auth.service;

import com.mymealserver.api.auth.dto.request.LoginRequest;
import com.mymealserver.api.auth.dto.request.LogoutRequest;
import com.mymealserver.api.auth.dto.request.RefreshRequest;
import com.mymealserver.api.auth.dto.request.RegisterRequest;
import com.mymealserver.api.auth.dto.request.WithdrawRequest;
import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.api.auth.dto.response.RefreshResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.security.JwtProvider;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsWriter;
import com.mymealserver.domain.memberwithdrawal.MemberWithdrawal;
import com.mymealserver.domain.memberwithdrawal.MemberWithdrawalRepository;
import com.mymealserver.external.redis.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final MemberReader memberReader;
  private final MemberWriter memberWriter;
  private final MemberSettingsWriter memberSettingsWriter;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;
  private final MemberWithdrawalRepository memberWithdrawalRepository;
  private final RedisTokenService redisTokenService;
  private final JwtProvider jwtProvider;

  @Transactional
  public void register(RegisterRequest request) {
    // 1. 이메일 중복 체크
    if (memberReader.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
    }

    // 2. 비밀번호 인코딩
    String encodedPassword = passwordEncoder.encode(request.password());

    // 3. Member 엔티티 생성 (DTO의 toEntity 메서드 사용)
    Member member = request.toEntity(encodedPassword);
    member = memberWriter.save(member);

    // 4. 기본 설정 생성
    MemberSettings settings = MemberSettings.createDefault(member);
    if (request.fcmToken() != null) {
      settings.updateFcmToken(request.fcmToken());
    }
    memberSettingsWriter.save(settings);
  }

  @Transactional
  public LoginResponse login(LoginRequest request) {
    // 1. 이메일로 회원 조회
    Member member = memberReader.findByEmail(request.email());

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), member.getPassword())) {
      throw BusinessException.error(ErrorCode.INVALID_CREDENTIALS);
    }

    // 3. 활성 상태 확인
    if (!member.isActive()) {
      throw BusinessException.error(ErrorCode.MEMBER_DEACTIVATED);
    }

    // 4. 마지막 로그인 시간 업데이트
    member.updateLastLoginAt();
    memberWriter.save(member);

    // 5. FCM 토큰 업데이트 (제공된 경우)
    if (request.fcmToken() != null) {
      memberSettingsWriter.updateFcmToken(member.getId(), request.fcmToken());
    }

    // 6. 토큰 생성
    return tokenService.generateToken(member);
  }

  public RefreshResponse reissueToken(RefreshRequest request) {
    String refreshToken = request.refreshToken();
    return tokenService.reissueToken(refreshToken);
  }

  @Transactional
  public void logout(Long memberId, LogoutRequest request) {
    // 1. 액세스 토큰 블랙
    String accessToken = request.accessToken();
    long expiration = jwtProvider.getExpiration(accessToken);
    redisTokenService.addBlacklist(accessToken, expiration);

    // 2. 리프레쉬 토큰 캐싱 삭제
    redisTokenService.deleteRefreshToken(memberId);
  }

  @Transactional
  public void withdraw(Long memberId, WithdrawRequest request) {

    Member member = memberReader.findById(memberId);

    // 1. 탈퇴 기록 생성 (DTO의 toEntity 메서드 사용)
    MemberWithdrawal withdrawal = request.toEntity(memberId);

    memberWithdrawalRepository.save(withdrawal);

    // 2. 회원 비활성화 및 소프트 삭제
    member.deactivate();
    memberWriter.delete(member);
  }
}
