package com.mymealserver.external.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.external.oauth.OAuth2UserInfo;

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
