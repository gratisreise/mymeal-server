package com.mymealserver.auth.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.auth.dto.AuthResponse;
import com.mymealserver.auth.dto.OAuthRequest;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.enums.ProviderType;
import com.mymealserver.repository.MemberSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;
    private final TokenService tokenService;
    private final MemberSettingsRepository memberSettingsRepository;

    @Transactional
    public AuthResponse oauthLogin(String provider, OAuthRequest request) {
        log.info("OAuth login attempt for provider: {}", provider);

        // 1. Validate provider type
        ProviderType providerType;
        try {
            providerType = ProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }

        // 2. Get OAuth2 user info from provider
        // TODO: Implement actual OAuth2 user info retrieval using Spring Security OAuth2 Client
        // For now, this is a placeholder that needs to be implemented with:
        // - Spring Security OAuth2 Client integration
        // - Provider-specific user info endpoints
        // - Token validation with providers
        OAuth2UserInfo userInfo = getOAuth2UserInfo(providerType, request.token());

        // 3. Find or create member
        Member member = findOrCreateMember(providerType, userInfo);

        // 4. Update last login
        member.updateLastLoginAt();
        memberWriter.save(member);

        // 5. Update FCM token
        if (request.fcmToken() != null) {
            updateFcmToken(member.getId(), request.fcmToken());
        }

        log.info("OAuth login successful for member: {}", member.getId());

        // 6. Generate tokens
        return tokenService.generateTokens(member);
    }

    private OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, String token) {
        // TODO: Implement actual OAuth2 user info retrieval
        // This is a placeholder that should:
        // 1. Validate the OAuth2 access token with the provider
        // 2. Call the provider's user info endpoint
        // 3. Parse and return user information

        // For now, throw an exception indicating that this needs to be implemented
        throw new BusinessException(ErrorCode.OAUTH_TOKEN_FAILED);

        // Example implementation structure:
        /*
        switch (providerType) {
            case GOOGLE:
                return getGoogleUserInfo(token);
            case NAVER:
                return getNaverUserInfo(token);
            case KAKAO:
                return getKakaoUserInfo(token);
            default:
                throw new BusinessException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        */
    }

    private Member findOrCreateMember(ProviderType providerType, OAuth2UserInfo userInfo) {
        // Try to find existing member by provider and providerId
        Member existingMember = memberReader.findByProviderAndProviderId(providerType, userInfo.id());

        if (existingMember != null) {
            log.info("Found existing member for provider {}: {}", providerType, existingMember.getId());
            return existingMember;
        }

        // Check if email is already registered with another provider
        if (userInfo.email() != null && memberReader.existsByEmail(userInfo.email())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
        }

        // Create new member
        Member newMember = Member.builder()
                .email(userInfo.email() != null ? userInfo.email() : userInfo.id() + "@" + providerType.getValue() + ".com")
                .name(userInfo.name() != null ? userInfo.name() : "User")
                .profileImage(userInfo.profileImage())
                .provider(providerType)
                .providerId(userInfo.id())
                .isActive(true)
                .build();

        newMember = memberWriter.save(newMember);

        // Create default settings
        MemberSettings settings = MemberSettings.createDefault(newMember);
        memberSettingsRepository.save(settings);

        log.info("Created new member for provider {}: {}", providerType, newMember.getId());
        return newMember;
    }

    private void updateFcmToken(Long memberId, String fcmToken) {
        MemberSettings settings = memberSettingsRepository.findByMemberId(memberId)
                .orElse(null);

        if (settings != null) {
            settings.updateFcmToken(fcmToken);
            memberSettingsRepository.save(settings);
        } else {
            Member member = memberReader.findById(memberId);
            MemberSettings newSettings = MemberSettings.createDefault(member);
            newSettings.updateFcmToken(fcmToken);
            memberSettingsRepository.save(newSettings);
        }
    }
}
