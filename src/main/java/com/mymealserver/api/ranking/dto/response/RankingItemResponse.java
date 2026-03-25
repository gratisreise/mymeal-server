package com.mymealserver.api.ranking.dto.response;

import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
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
    public static RankingItemResponse of(
            Integer rank,
            Long mealId,
            String mealName,
            String photoUrl,
            LocalDateTime mealTime,
            Double overallScore,
            GradeType grade,
            MealType mealType
    ) {
        return new RankingItemResponse(rank, mealId, mealName, photoUrl, mealTime, overallScore, grade, mealType);
    }
}
