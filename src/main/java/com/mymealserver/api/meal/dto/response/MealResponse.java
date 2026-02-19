package com.mymealserver.api.meal.dto.response;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

public record MealResponse(
        Long id,
        MealType mealType,
        LocalDateTime mealTime,
        String photoUrl,
        Boolean hasReaction,
        LocalDateTime createdAt
) {
    public static MealResponse from(Meal meal, boolean hasReaction) {
        return new MealResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getMealTime(),
                meal.getPhotoUrl(),
                hasReaction,
                meal.getCreatedAt()
        );
    }
}
