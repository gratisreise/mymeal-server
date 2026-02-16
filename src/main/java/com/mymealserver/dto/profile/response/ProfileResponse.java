package com.mymealserver.dto.profile;

import java.time.LocalDateTime;

public record ProfileResponse(
        Long id,
        String email,
        String name,
        String profileImage,
        LocalDateTime createdAt
) {
}
