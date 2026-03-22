package com.mymealserver.api.recommendation.service;

/**
 * AI 기반 식단 추천 응답 DTO
 * ChatClient.entity()로 JSON 파싱하기 위한 record
 */
public record RecommendationResult(
        MealRecommendation breakfast,
        MealRecommendation lunch,
        MealRecommendation dinner
) {
    public record MealRecommendation(
            String mealName,
            String reason,
            String nutritionTips
    ) {}
}
