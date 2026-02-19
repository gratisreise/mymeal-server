package com.mymealserver.api.meal.dto.response;

import com.mymealserver.entity.MealAnalysis;

import java.util.List;

public record AIAnalysisResponse(
        String mealName,
        Double calories,
        NutrientsResponse nutrients,
        List<String> tags,
        Double confidence
) {
    public record NutrientsResponse(
            Double carbohydrates,
            Double protein,
            Double fat,
            Double fiber
    ) {
    }

    public static AIAnalysisResponse from(MealAnalysis analysis) {
        return new AIAnalysisResponse(
                analysis.getMealName(),
                analysis.getCalories(),
                new NutrientsResponse(
                        analysis.getCarbohydrates(),
                        analysis.getProtein(),
                        analysis.getFat(),
                        0.0
                ),
                List.of(),
                analysis.getConfidence()
        );
    }
}
