package com.mymealserver.api.profile.dto.response;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String profileImage,
        LocalDateTime createdAt
) {
}
