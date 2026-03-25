package com.mymealserver.external.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.oauth.OAuth2UserInfo;


public record KakaoUserInfoResponse(
        String id,

        @JsonProperty("kakao_account")
        KakaoProfile kakaoAccount
) implements OAuth2UserInfo {

    public record KakaoProfile(
        @JsonProperty("profile_nickname")
        String nickname,

        @JsonProperty("profile_image_url")
        String profileImageUrl
    ) {
    }

    @Override
    public String name() {
        return kakaoAccount != null ? kakaoAccount.nickname() : null;
    }

    @Override
    public String profileImage() {
        return kakaoAccount != null ? kakaoAccount.profileImageUrl() : null;
    }

    @Override
    public Member toEntity() {
        return Member.builder()
            .email(this.id() + "@kakao.com")
            .name(this.name() != null ? this.name() : "User")
            .profileImage(this.profileImage())
            .provider(ProviderType.KAKAO)
            .providerId(this.id())
            .isActive(true)
            .build();
    }
}
