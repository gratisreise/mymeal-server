package com.mymealserver.api.recommendation.dto.response;

import com.mymealserver.common.enums.MealType;
import java.time.LocalDateTime;

public record RecommendationScheduleResponse(
        MealType mealType,
        LocalDateTime recommendedTime,
        String mealName,
        String reason
) {
}
