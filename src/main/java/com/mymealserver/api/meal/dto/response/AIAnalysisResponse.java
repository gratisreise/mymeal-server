package com.mymealserver.api.meal.dto.response;

import com.mymealserver.domain.mealanalysis.MealAnalysis;
import java.util.List;

public record AIAnalysisResponse(
        String mealName,
        Double calories,
        NutrientsResponse nutrients,
        List<String> tags,
        Double confidence
) {

    public static AIAnalysisResponse from(MealAnalysis analysis) {
        return new AIAnalysisResponse(
                analysis.getMealName(),
                analysis.getCalories(),
                NutrientsResponse.from(analysis),
                List.of(),
                analysis.getConfidence()
        );
    }

    public record NutrientsResponse(
        Double carbohydrates,
        Double protein,
        Double fat,
        Double fiber
    ) {
        private static NutrientsResponse from(MealAnalysis analysis){
            return new NutrientsResponse(
                analysis.getCarbohydrates(),
                analysis.getProtein(),
                analysis.getFat(),
                0.0
            );
        }
    }
}
