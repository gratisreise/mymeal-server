package com.mymealserver.dto.auth;

import com.mymealserver.dto.profile.MemberResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        MemberResponse member
) {
}
