package com.mymealserver.auth.service.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.auth.service.OAuth2UserInfo;

/**
 * Kakao OAuth 사용자 정보 응답 래퍼
 */
public record KakaoUserInfoResponse(
        String id,

        @JsonProperty("kakao_account")
        KakaoProfile kakaoAccount
) implements OAuth2UserInfo {

    @Override
    public String name() {
        return kakaoAccount != null ? kakaoAccount.nickname() : null;
    }

    @Override
    public String profileImage() {
        return kakaoAccount != null ? kakaoAccount.profileImageUrl() : null;
    }
}
