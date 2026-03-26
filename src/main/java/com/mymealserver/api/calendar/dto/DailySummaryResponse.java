package com.mymealserver.api.calendar.dto;

import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import java.util.List;

public record DailySummaryResponse(
    Integer mealCount, List<MealType> mealTypes, Double averageScore, GradeType quality) {}
