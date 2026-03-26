package com.mymealserver.api.recommendation.service.dto;

public record RecommendationResult(
    MealRecommendation breakfast, MealRecommendation lunch, MealRecommendation dinner) {

  public static RecommendationResult of(
      MealRecommendation breakfast, MealRecommendation lunch, MealRecommendation dinner) {
    return new RecommendationResult(breakfast, lunch, dinner);
  }

  public record MealRecommendation(String mealName, String reason, String nutritionTips) {

    public static MealRecommendation of(String mealName, String reason, String nutritionTips) {
      return new MealRecommendation(mealName, reason, nutritionTips);
    }
  }
}
