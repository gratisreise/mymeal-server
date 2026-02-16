package com.mymealserver.calendar.dto;

import com.mymealserver.dto.meal.MealDetailResponse;

import java.time.LocalDate;
import java.util.List;

public record CalendarDailyResponse(
        LocalDate date,
        StatisticsResponse statistics,
        List<MealDetailResponse> meals
) {
    public record StatisticsResponse(
            Integer mealCount,
            Double averageScore,
            Double reactionRate
    ) {
    }
}
