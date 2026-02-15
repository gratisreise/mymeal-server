package com.mymealserver.dto.calendar;

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
