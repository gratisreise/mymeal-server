package com.mymealserver.dto.calendar;

import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;

import java.util.List;

public record DailySummaryResponse(
        Integer mealCount,
        List<MealType> mealTypes,
        Double averageScore,
        GradeType quality
) {
}
