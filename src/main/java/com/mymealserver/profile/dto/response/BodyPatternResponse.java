package com.mymealserver.profile.dto.response;

import java.util.List;

public record BodyPatternResponse(
        List<BodyPatternTagResponse> goodTags,
        List<BodyPatternTagResponse> badTags,
        Double overallAverageScore
) {
}
