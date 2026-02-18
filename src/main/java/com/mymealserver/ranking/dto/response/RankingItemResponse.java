package com.mymealserver.ranking.dto.response;

import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

public record RankingItemResponse(
        Integer rank,
        Long mealId,
        String mealName,
        String photoUrl,
        LocalDateTime mealTime,
        Double overallScore,
        GradeType grade,
        MealType mealType
) {
}
