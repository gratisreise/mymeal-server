package com.mymealserver.api.recommendation.service;

public record FoodAnalysisResult(
        String mealName,
        Double calories,
        Double carbohydrates,
        Double protein,
        Double fat,
        Double confidence
) {
}
