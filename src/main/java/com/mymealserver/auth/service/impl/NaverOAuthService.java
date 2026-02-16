package com.mymealserver.auth.service.impl;

import com.mymealserver.auth.dto.request.OAuthRequest;
import com.mymealserver.auth.dto.response.AuthResponse;
import com.mymealserver.auth.service.OAuthService;
import com.mymealserver.auth.service.TokenService;
import com.mymealserver.auth.service.client.naver.NaverApiClient;
import com.mymealserver.auth.service.client.naver.NaverTokenResponse;
import com.mymealserver.auth.service.client.naver.NaverUserInfoResponse;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.member.MemberSettingsWriter;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.enums.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Naver OAuth 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements OAuthService {

    private final NaverApiClient apiClient;
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final MemberSettingsWriter memberSettingsWriter;
    private final TokenService tokenService;

    @Override
    @Transactional
    public AuthResponse authenticate(OAuthRequest request) {
        log.info("Naver OAuth 인증 시도");

        // 1. 인증 코드를 액세스 토큰으로 교환
        // ApiClient가 내부적으로 @Value로 주입받은 redirectUri 사용
        NaverTokenResponse tokenResponse = apiClient.exchangeCodeForToken(request.code());

        // 2. 제공업체에서 사용자 정보 조회
        NaverUserInfoResponse userInfo = apiClient.getUserInfo(tokenResponse.accessToken());

        // 3. 회원 조회 또는 생성
        Member member = getOrCreateMember(userInfo);

        // 4. 마지막 로그인 시간 업데이트
        member.updateLastLoginAt();
        memberWriter.save(member);

        // 5. FCM 토큰 업데이트 (제공된 경우)
        if (request.fcmToken() != null) {
            memberSettingsWriter.updateFcmToken(member.getId(), request.fcmToken());
        }

        log.info("Naver OAuth 인증 성공 - 회원 ID: {}", member.getId());

        // 6. JWT 토큰 생성
        return tokenService.generateTokens(member);
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.NAVER;
    }

    private Member getOrCreateMember(NaverUserInfoResponse userInfoResponse) {
        // provider + providerId로 기존 회원 조회
        Member existingMember = memberReader.findByProviderAndProviderId(
                ProviderType.NAVER,
                userInfoResponse.id()
        );

        if (existingMember != null) {
            log.info("기존 회원 찾음 (NAVER): {}", existingMember.getId());
            return existingMember;
        }

        // 신규 회원 생성 (email은 providerId@provider.com 형태)
        Member newMember = Member.builder()
                .email(userInfoResponse.id() + "@naver.com")
                .name(userInfoResponse.name() != null ? userInfoResponse.name() : "User")
                .profileImage(userInfoResponse.profileImage())
                .provider(ProviderType.NAVER)
                .providerId(userInfoResponse.id())
                .isActive(true)
                .build();

        newMember = memberWriter.save(newMember);

        // 기본 설정 생성
        memberSettingsWriter.createDefault(newMember);

        log.info("신규 회원 생성 (NAVER): {}", newMember.getId());
        return newMember;
    }
}
