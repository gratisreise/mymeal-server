package com.mymealserver.auth.service.client.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Naver OAuth token response
 */
public record NaverTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
