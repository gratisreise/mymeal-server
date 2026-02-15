package com.mymealserver.dto.profile;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String profileImage,
        LocalDateTime createdAt
) {
}
