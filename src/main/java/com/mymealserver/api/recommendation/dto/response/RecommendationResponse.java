package com.mymealserver.recommendation.dto.response;

import java.util.List;

public record RecommendationResponse(
        String mealName,
        String reason,
        List<String> matchingTags,
        Double averageScore,
        Long referenceMealId
) {
}
