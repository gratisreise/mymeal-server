package com.mymealserver.api.profile.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "{validation.name.size.max}")
        String name,
        String profileImage
) {
}
