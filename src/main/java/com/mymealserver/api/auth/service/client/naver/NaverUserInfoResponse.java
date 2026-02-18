package com.mymealserver.auth.service.client.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.auth.service.OAuth2UserInfo;

/**
 * Naver OAuth 사용자 정보 응답 래퍼
 */
public record NaverUserInfoResponse(
        @JsonProperty("response")
        NaverProfile response
) implements OAuth2UserInfo {

    @Override
    public String id() {
        return response != null ? response.id() : null;
    }

    @Override
    public String name() {
        return response != null ? response.nickname() : null;
    }

    @Override
    public String profileImage() {
        return response != null ? response.profileImage() : null;
    }
}
