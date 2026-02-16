package com.mymealserver.dto.meal;

import com.mymealserver.entity.enums.MealType;
import com.mymealserver.dto.reaction.ReactionResponse;

import java.time.LocalDateTime;

public record MealDetailResponse(
        Long id,
        MealType mealType,
        LocalDateTime mealTime,
        String photoUrl,
        String memo,
        Boolean hasReaction,
        LocalDateTime createdAt,
        AIAnalysisResponse aiAnalysis,
        ReactionResponse reaction
) {
}
