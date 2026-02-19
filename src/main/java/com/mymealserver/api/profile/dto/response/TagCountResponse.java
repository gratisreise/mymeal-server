package com.mymealserver.api.profile.dto.response;

public record TagCountResponse(
        String tag,
        Integer count
) {
}
