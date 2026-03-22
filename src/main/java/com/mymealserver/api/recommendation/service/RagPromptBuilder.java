package com.mymealserver.api.recommendation.service;

import com.mymealserver.domain.meallog.MealLog;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RagPromptBuilder {

    private static final String RECOMMENDATION_PROMPT_TEMPLATE = """
            You are a personal dietitian. Analyze the user's most positive meal experiences to recommend today's meals.

            ## Top 3 Most Positive Meals (Vector Similarity Search Results)
            %s

            ## Response Format (JSON)
            Generate a JSON with the following structure for breakfast, lunch, and dinner:

            {
              "breakfast": {
                "mealName": "Recommended meal name",
                "reason": "Reason based on past reactions (max 50 chars)",
                "nutritionTips": "Nutrition tips (max 30 chars)"
              },
              "lunch": {
                "mealName": "Recommended meal name",
                "reason": "Reason based on past reactions (max 50 chars)",
                "nutritionTips": "Nutrition tips (max 30 chars)"
              },
              "dinner": {
                "mealName": "Recommended meal name",
                "reason": "Reason based on past reactions (max 50 chars)",
                "nutritionTips": "Nutrition tips (max 30 chars)"
              }
            }

            Respond with JSON only. All text fields must be in Korean. No additional explanation.
            """;

    private RagPromptBuilder() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    public static String buildRecommendationPrompt(List<MealLog> goodMeals) {
        String goodMealsSection = buildGoodMealsSection(goodMeals);
        return RECOMMENDATION_PROMPT_TEMPLATE.formatted(goodMealsSection);
    }

    private static String buildGoodMealsSection(List<MealLog> goodMeals) {
        StringBuilder section = new StringBuilder();
        for (MealLog mealLog : goodMeals) {
            section.append("- %s\n".formatted(mealLog.getCombinedSummary()));
        }
        return section.toString().trim();
    }
}
