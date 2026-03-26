package com.mymealserver.api.meal.dto.response;

import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import java.time.LocalDateTime;

public record MealResponse(
    Long id,
    MealType mealType,
    LocalDateTime mealTime,
    String photoUrl,
    Boolean hasReaction,
    LocalDateTime createdAt) {
  public static MealResponse from(Meal meal, boolean hasReaction) {
    return new MealResponse(
        meal.getId(),
        meal.getMealType(),
        meal.getMealTime(),
        meal.getPhotoUrl(),
        hasReaction,
        meal.getCreatedAt());
  }
}
