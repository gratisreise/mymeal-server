package com.mymealserver.api.calendar.dto;

import com.mymealserver.api.meal.dto.response.MealDetailResponse;

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
