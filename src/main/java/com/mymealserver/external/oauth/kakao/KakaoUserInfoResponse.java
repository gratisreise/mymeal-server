package com.mymealserver.external.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.oauth.OAuth2UserInfo;

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
