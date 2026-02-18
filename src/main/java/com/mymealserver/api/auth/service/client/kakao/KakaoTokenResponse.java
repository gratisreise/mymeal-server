package com.mymealserver.auth.service.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kakao OAuth token response
 */
public record KakaoTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("refresh_token_expires_in")
        Long refreshTokenExpiresIn
) {
}
