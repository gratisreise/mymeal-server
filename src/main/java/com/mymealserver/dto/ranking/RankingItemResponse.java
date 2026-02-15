package com.mymealserver.dto.ranking;

import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

public record RankingItemResponse(
        Integer rank,
        Long mealId,
        String mealName,
        String photoUrl,
        LocalDateTime mealTime,
        Double overallScore,
        MealType mealType
) {
}
