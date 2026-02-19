package com.mymealserver.common.test.fixtures;

import com.mymealserver.api.auth.service.client.google.GoogleTokenResponse;
import com.mymealserver.api.auth.service.client.google.GoogleUserInfoResponse;
import com.mymealserver.api.auth.service.client.kakao.KakaoProfile;
import com.mymealserver.api.auth.service.client.kakao.KakaoTokenResponse;
import com.mymealserver.api.auth.service.client.kakao.KakaoUserInfoResponse;
import com.mymealserver.api.auth.service.client.naver.NaverProfile;
import com.mymealserver.api.auth.service.client.naver.NaverTokenResponse;
import com.mymealserver.api.auth.service.client.naver.NaverUserInfoResponse;

/**
 * Test fixture for OAuth-related test data
 * Provides reusable OAuth API response objects for testing
 */
public class OAuthFixture {

    // Test authorization codes
    public static final String VALID_AUTH_CODE = "valid_authorization_code_abc123";
    public static final String INVALID_AUTH_CODE = "invalid_authorization_code_xyz789";

    // Test access tokens from OAuth providers
    public static final String GOOGLE_ACCESS_TOKEN = "google_access_token_ghi012";
    public static final String KAKAO_ACCESS_TOKEN = "kakao_access_token_jkl345";
    public static final String NAVER_ACCESS_TOKEN = "naver_access_token_mno678";

    // Provider-specific IDs
    public static final String GOOGLE_USER_ID = "123456789";
    public static final String KAKAO_USER_ID = "987654321";
    public static final String NAVER_USER_ID = "456789123";

    // User names
    public static final String GOOGLE_USER_NAME = "Google User";
    public static final String KAKAO_USER_NAME = "Kakao User";
    public static final String NAVER_USER_NAME = "Naver User";

    // Profile images
    public static final String GOOGLE_PROFILE_IMAGE = "https://example.com/google-profile.jpg";
    public static final String KAKAO_PROFILE_IMAGE = "https://example.com/kakao-profile.jpg";
    public static final String NAVER_PROFILE_IMAGE = "https://example.com/naver-profile.jpg";

    // ========================================================================
    // Google OAuth Fixtures
    // ========================================================================

    /**
     * Creates a valid Google token response
     */
    public static GoogleTokenResponse createGoogleTokenResponse() {
        return new GoogleTokenResponse(
                GOOGLE_ACCESS_TOKEN,
                "Bearer",
                3600L,
                "google_refresh_token",
                "openid profile email",
                "google_id_token"
        );
    }

    /**
     * Creates a valid Google user info response with all fields
     */
    public static GoogleUserInfoResponse createGoogleUserInfo() {
        return new GoogleUserInfoResponse(
                GOOGLE_USER_ID,
                GOOGLE_USER_NAME,
                GOOGLE_PROFILE_IMAGE
        );
    }

    /**
     * Creates a Google user info response with null name (edge case)
     */
    public static GoogleUserInfoResponse createGoogleUserInfoWithNullName() {
        return new GoogleUserInfoResponse(
                GOOGLE_USER_ID,
                null,
                GOOGLE_PROFILE_IMAGE
        );
    }

    /**
     * Creates a Google user info response with minimal fields
     */
    public static GoogleUserInfoResponse createGoogleUserInfoMinimal() {
        return new GoogleUserInfoResponse(
                GOOGLE_USER_ID,
                null,
                null
        );
    }

    // ========================================================================
    // Kakao OAuth Fixtures
    // ========================================================================

    /**
     * Creates a valid Kakao token response
     */
    public static KakaoTokenResponse createKakaoTokenResponse() {
        return new KakaoTokenResponse(
                KAKAO_ACCESS_TOKEN,
                "Bearer",
                3600L,
                "kakao_refresh_token",
                "account_email profile",
                5184000L
        );
    }

    /**
     * Creates a valid Kakao profile
     */
    public static KakaoProfile createKakaoProfile() {
        return new KakaoProfile(
                KAKAO_USER_NAME,
                KAKAO_PROFILE_IMAGE
        );
    }

    /**
     * Creates a valid Kakao user info response
     */
    public static KakaoUserInfoResponse createKakaoUserInfo() {
        return new KakaoUserInfoResponse(
                KAKAO_USER_ID,
                createKakaoProfile()
        );
    }

    /**
     * Creates a Kakao user info response with null profile
     */
    public static KakaoUserInfoResponse createKakaoUserInfoWithNullProfile() {
        return new KakaoUserInfoResponse(
                KAKAO_USER_ID,
                null
        );
    }

    /**
     * Creates a Kakao user info response with null nickname
     */
    public static KakaoUserInfoResponse createKakaoUserInfoWithNullNickname() {
        return new KakaoUserInfoResponse(
                KAKAO_USER_ID,
                new KakaoProfile(
                        null,
                        KAKAO_PROFILE_IMAGE
                )
        );
    }

    // ========================================================================
    // Naver OAuth Fixtures
    // ========================================================================

    /**
     * Creates a valid Naver token response
     */
    public static NaverTokenResponse createNaverTokenResponse() {
        return new NaverTokenResponse(
                NAVER_ACCESS_TOKEN,
                "Bearer",
                3600L,
                "naver_refresh_token"
        );
    }

    /**
     * Creates a valid Naver profile
     */
    public static NaverProfile createNaverProfile() {
        return new NaverProfile(
                NAVER_USER_ID,
                NAVER_USER_NAME,
                NAVER_PROFILE_IMAGE,
                null  // name field is not used
        );
    }

    /**
     * Creates a valid Naver user info response
     */
    public static NaverUserInfoResponse createNaverUserInfo() {
        return new NaverUserInfoResponse(
                createNaverProfile()
        );
    }

    /**
     * Creates a Naver user info response with null response
     */
    public static NaverUserInfoResponse createNaverUserInfoWithNullResponse() {
        return new NaverUserInfoResponse(
                null
        );
    }

    /**
     * Creates a Naver user info response with null nickname
     */
    public static NaverUserInfoResponse createNaverUserInfoWithNullNickname() {
        return new NaverUserInfoResponse(
                new NaverProfile(
                        NAVER_USER_ID,
                        null,
                        NAVER_PROFILE_IMAGE,
                        null
                )
        );
    }
}
