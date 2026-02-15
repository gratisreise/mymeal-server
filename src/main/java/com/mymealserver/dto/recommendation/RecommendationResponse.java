package com.mymealserver.dto.recommendation;

import java.util.List;

public record RecommendationResponse(
        String mealName,
        String reason,
        List<String> matchingTags,
        Double averageScore,
        Long referenceMealId
) {
}
