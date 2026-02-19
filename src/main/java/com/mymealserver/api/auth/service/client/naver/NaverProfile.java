package com.mymealserver.api.auth.service.client.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Naver response 객체 내 프로필 정보
 */
public record NaverProfile(
        String id,

        String nickname,

        @JsonProperty("profile_image")
        String profileImage,

        String name
) {
}
