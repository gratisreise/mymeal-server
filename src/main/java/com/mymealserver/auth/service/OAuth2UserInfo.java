package com.mymealserver.auth.service;

import lombok.Builder;

@Builder
public record OAuth2UserInfo(

        String id,

        String email,

        String name,

        String profileImage

) {
}
