package com.mymealserver.dto.profile;

import java.util.List;

public record BodyPatternResponse(
        List<BodyPatternTagResponse> goodTags,
        List<BodyPatternTagResponse> badTags,
        Double overallAverageScore
) {
}
