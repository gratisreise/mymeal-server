package com.mymealserver.external.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.oauth.OAuth2UserInfo;

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

    @Override
    public Member toEntity() {
        return Member.builder()
            .email(this.id() + "@naver.com")
            .name(this.name() != null ? this.name() : "User")
            .profileImage(this.profileImage())
            .provider(ProviderType.NAVER)
            .providerId(this.id())
            .isActive(true)
            .build();
    }
}
