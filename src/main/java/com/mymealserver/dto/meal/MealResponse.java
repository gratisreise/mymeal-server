package com.mymealserver.dto.meal;

import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

public record MealResponse(
        Long id,
        MealType mealType,
        LocalDateTime mealTime,
        String photoUrl,
        String memo,
        Boolean hasReaction,
        LocalDateTime createdAt
) {
}
