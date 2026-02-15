package com.mymealserver.dto.profile;

public record BodyPatternTagResponse(
        String tag,
        Double averageScore,
        Integer count
) {
}
