package com.mymealserver.profile.dto.response;

public record BodyPatternTagResponse(
        String tag,
        Double averageScore,
        Integer count
) {
}
