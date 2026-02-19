package com.mymealserver.api.profile.dto.response;

public record BodyPatternTagResponse(
        String tag,
        Double averageScore,
        Integer count
) {
}
