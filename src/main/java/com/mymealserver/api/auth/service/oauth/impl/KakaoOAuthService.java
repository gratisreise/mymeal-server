package com.mymealserver.api.auth.service.oauth.impl;

import com.mymealserver.api.auth.dto.request.OAuthRequest;
import com.mymealserver.api.auth.dto.response.LoginResponse;
import com.mymealserver.api.auth.service.TokenService;
import com.mymealserver.api.auth.service.oauth.OAuthService;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.membersettings.MemberSettingsWriter;
import com.mymealserver.external.oauth.kakao.KakaoApiClient;
import com.mymealserver.external.oauth.kakao.KakaoTokenResponse;
import com.mymealserver.external.oauth.kakao.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

  private final KakaoApiClient apiClient;
  private final MemberReader memberReader;
  private final MemberWriter memberWriter;
  private final MemberSettingsWriter memberSettingsWriter;
  private final TokenService tokenService;

  @Override
  @Transactional
  public LoginResponse authenticate(OAuthRequest request) {

    // 1. 인증 코드를 액세스 토큰으로 교환
    KakaoTokenResponse tokenResponse = apiClient.exchangeCodeForToken(request.code());

    // 2. 제공업체에서 사용자 정보 조회
    KakaoUserInfoResponse userInfo = apiClient.getUserInfo(tokenResponse.accessToken());

    // 3. 회원 조회 또는 생성
    Member member = getOrCreateMember(userInfo);

    // 4. 마지막 로그인 시간 업데이트
    member.updateLastLoginAt();
    memberWriter.save(member);

    // 5. FCM 토큰 업데이트 (제공된 경우)
    if (request.fcmToken() != null) {
      memberSettingsWriter.updateFcmToken(member.getId(), request.fcmToken());
    }

    // 6. JWT 토큰 생성
    return tokenService.generateToken(member);
  }

  @Override
  public ProviderType getProvider() {
    return ProviderType.KAKAO;
  }

  private Member getOrCreateMember(KakaoUserInfoResponse userInfo) {
    // provider + providerId로 기존 회원 조회
    Member existingMember =
        memberReader.findByProviderAndProviderId(ProviderType.KAKAO, userInfo.id());

    if (existingMember != null) {
      return existingMember;
    }

    Member newMember = userInfo.toEntity();

    newMember = memberWriter.save(newMember);

    // 기본 설정 생성
    memberSettingsWriter.createDefault(newMember);

    return newMember;
  }
}
