package com.mymealserver.api.meal.dto.response;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;

import java.time.LocalDateTime;

public record MealDetailResponse(
        Long id,
        MealType mealType,
        LocalDateTime mealTime,
        String photoUrl,
        Boolean hasReaction,
        LocalDateTime createdAt,
        AIAnalysisResponse aiAnalysis,
        ReactionResponse reaction
) {
    public static MealDetailResponse from(
            Meal meal,
            AIAnalysisResponse aiAnalysis,
            ReactionResponse reaction,
            boolean hasReaction
    ) {
        return new MealDetailResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getMealTime(),
                meal.getPhotoUrl(),
                hasReaction,
                meal.getCreatedAt(),
                aiAnalysis,
                reaction
        );
    }
}
