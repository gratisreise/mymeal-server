package com.mymealserver.external.oauth;

import com.mymealserver.domain.member.Member;

public interface OAuth2UserInfo {

    String id();

    String name();

    String profileImage();

    Member toEntity();
}
