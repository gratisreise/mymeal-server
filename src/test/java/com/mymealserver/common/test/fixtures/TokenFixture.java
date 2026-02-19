package com.mymealserver.common.test.fixtures;

import com.mymealserver.api.auth.dto.response.AuthResponse;
import com.mymealserver.api.auth.dto.response.MemberResponse;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.enums.ProviderType;

import java.time.LocalDateTime;

/**
 * Test fixture for token-related test data
 * Provides reusable tokens and AuthResponse instances for testing
 */
public class TokenFixture {

    // Test tokens
    public static final String VALID_ACCESS_TOKEN = "valid_access_token_abc123";
    public static final String VALID_REFRESH_TOKEN = "valid_refresh_token_xyz789";
    public static final String BLACKLISTED_REFRESH_TOKEN = "blacklisted_refresh_token_def456";
    public static final String EXPIRED_ACCESS_TOKEN = "expired_access_token_ghi012";
    public static final String EXPIRED_REFRESH_TOKEN = "expired_refresh_token_jkl345";
    public static final String INVALID_TOKEN = "invalid_token_mno678";
    public static final String MALFORMED_TOKEN = "malformed.token.string";

    // Token metadata
    public static final Long MEMBER_ID = 1L;
    public static final Long EXPIRED_MEMBER_ID = 999L;

    /**
     * Creates a valid AuthResponse with mock tokens
     */
    public static AuthResponse createAuthResponse() {
        MemberResponse memberResponse = MemberResponse.builder()
                .id(MEMBER_ID)
                .email("test@example.com")
                .name("Test User")
                .profileImage(null)
                .provider(ProviderType.EMAIL)
                .isActive(true)
                .lastLoginAt(LocalDateTime.now())
                .build();

        return AuthResponse.builder()
                .accessToken(VALID_ACCESS_TOKEN)
                .refreshToken(VALID_REFRESH_TOKEN)
                .member(memberResponse)
                .build();
    }

    /**
     * Creates an AuthResponse with custom member
     */
    public static AuthResponse createAuthResponse(Member member) {
        MemberResponse memberResponse = MemberResponse.from(member);

        return AuthResponse.builder()
                .accessToken(VALID_ACCESS_TOKEN)
                .refreshToken(VALID_REFRESH_TOKEN)
                .member(memberResponse)
                .build();
    }

    /**
     * Creates an AuthResponse with custom tokens
     */
    public static AuthResponse createAuthResponse(String accessToken, String refreshToken, Member member) {
        MemberResponse memberResponse = MemberResponse.from(member);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(memberResponse)
                .build();
    }

    /**
     * Returns an expired access token string
     */
    public static String createExpiredAccessToken() {
        return EXPIRED_ACCESS_TOKEN;
    }

    /**
     * Returns an expired refresh token string
     */
    public static String createExpiredRefreshToken() {
        return EXPIRED_REFRESH_TOKEN;
    }

    /**
     * Returns a blacklisted refresh token string
     */
    public static String createBlacklistedRefreshToken() {
        return BLACKLISTED_REFRESH_TOKEN;
    }

    /**
     * Returns a valid (non-blacklisted) refresh token string
     */
    public static String createValidRefreshToken() {
        return VALID_REFRESH_TOKEN;
    }

    /**
     * Returns a valid access token string
     */
    public static String createValidAccessToken() {
        return VALID_ACCESS_TOKEN;
    }

    /**
     * Returns an invalid/malformed token string
     */
    public static String createInvalidToken() {
        return INVALID_TOKEN;
    }
}
