package com.mymealserver.dto.recommendation;

import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

public record RecommendationScheduleResponse(
        MealType mealType,
        LocalDateTime recommendedTime,
        String mealName,
        String reason
) {
}
