package com.mymealserver.profile.dto.response;

public record TagCountResponse(
        String tag,
        Integer count
) {
}
