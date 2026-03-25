package com.mymealserver.api.recommendation.service;


public record FoodAnalysisResult(
        String mealName,
        Double calories,
        Double carbohydrates,
        Double protein,
        Double fat,
        Double confidence
) {

    public static FoodAnalysisResult of(String mealName, Double calories, Double carbohydrates,
                                         Double protein, Double fat, Double confidence) {
        return new FoodAnalysisResult(mealName, calories, carbohydrates, protein, fat, confidence);
    }
}
