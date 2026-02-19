package com.mymealserver.api.auth.service.client.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.api.auth.service.OAuth2UserInfo;

/**
 * Google OAuth 사용자 정보 응답
 */
public record GoogleUserInfoResponse(
        String id,

        String name,

        @JsonProperty("picture")
        String profileImage
) implements OAuth2UserInfo {
}
