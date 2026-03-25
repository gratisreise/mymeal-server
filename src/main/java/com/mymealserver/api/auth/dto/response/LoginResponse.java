package com.mymealserver.api.auth.dto.response;

import com.mymealserver.domain.member.Member;
import lombok.Builder;

@Builder
public record LoginResponse(

        String accessToken,

        String refreshToken,

        MemberResponse member

) {

    public static LoginResponse of(String accessToken, String refreshToken, MemberResponse member) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .member(member)
            .build();
    }

}
