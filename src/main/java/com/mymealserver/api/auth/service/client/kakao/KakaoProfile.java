package com.mymealserver.api.auth.service.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kakao 카카오계정 내 프로필 정보
 */
public record KakaoProfile(
        @JsonProperty("profile_nickname")
        String nickname,

        @JsonProperty("profile_image_url")
        String profileImageUrl
) {
}
