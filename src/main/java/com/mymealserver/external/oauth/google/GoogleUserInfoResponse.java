package com.mymealserver.external.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.domain.member.Member;
import com.mymealserver.external.oauth.OAuth2UserInfo;


public record GoogleUserInfoResponse(
        String id,

        String name,

        @JsonProperty("picture")
        String profileImage
) implements OAuth2UserInfo {

        @Override
        public Member toEntity() {
                return Member.builder()
                    .email(this.id() + "@google.com")
                    .name(this.name() != null ? this.name() : "User")
                    .profileImage(this.profileImage())
                    .provider(ProviderType.GOOGLE)
                    .providerId(this.id())
                    .isActive(true)
                    .build();
        }

}
