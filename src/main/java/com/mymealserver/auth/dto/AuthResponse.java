package com.mymealserver.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(

        String accessToken,

        String refreshToken,

        MemberResponse member

) {
}
