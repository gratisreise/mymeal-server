package com.mymealserver.dto.profile;

public record TagCountResponse(
        String tag,
        Integer count
) {
}
