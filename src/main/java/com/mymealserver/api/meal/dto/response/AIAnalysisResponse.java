package com.mymealserver.meal.dto.response;

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

    /**
     * MealAnalysis 엔티티를 AIAnalysisResponse DTO로 변환
     *
     * @param analysis MealAnalysis 엔티티
     * @return AIAnalysisResponse DTO
     */
    public static AIAnalysisResponse from(MealAnalysis analysis) {
        return new AIAnalysisResponse(
                analysis.getMealName(),
                analysis.getCalories(),
                new NutrientsResponse(
                        analysis.getCarbohydrates(),
                        analysis.getProtein(),
                        analysis.getFat(),
                        0.0  // fiber - MealAnalysis 엔티티에 필드 없으면 기본값 0 반환
                ),
                List.of(),  // tags - MealAnalysis 엔티티에 필드 없으면 빈 리스트 반환
                analysis.getConfidence()
        );
    }
}
