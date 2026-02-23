package com.mymealserver.service.recommendation;

import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.MealAnalysis.MealAnalysis;
import com.mymealserver.domain.reaction.Reaction;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Builder
public class RagPromptBuilder {

    private final Member member;
    private final List<MealWithReaction> goodMeals;

    public String buildRecommendationPrompt() {
        StringBuilder prompt = new StringBuilder();

        // System prompt - Nutritionist persona
        prompt.append("당신은 개인 식단 영양사입니다. 사용자의 과거 식사 이력과 반응을 분석하여 오늘의 식단을 추천해주세요.\n\n");

        // User information section
        appendUserInfo(prompt);

        // Past good meals section
        appendGoodMeals(prompt);

        // Response format instructions
        appendResponseFormat(prompt);

        String result = prompt.toString();
        log.debug("Generated RAG prompt for memberId: {}, prompt length: {}",
                member.getId(), result.length());

        return result;
    }

    private void appendUserInfo(StringBuilder prompt) {
        prompt.append("## 사용자 정보\n");
        prompt.append(String.format("- 이름: %s\n", member.getName()));
        prompt.append(String.format("- 이메일: %s\n", member.getEmail()));

        // Future expansion fields (allergies, goal calories, etc.)
        // if (member.getAllergies() != null) {
        //     prompt.append(String.format("- 알러지: %s\n", member.getAllergies()));
        // }
        // if (member.getGoalCalories() != null) {
        //     prompt.append(String.format("- 목표 칼로리: %dkcal\n", member.getGoalCalories()));
        // }

        prompt.append("\n");
    }

    private void appendGoodMeals(StringBuilder prompt) {
        prompt.append("## 최근 30일 좋은 반응이었던 식사\n");

        if (goodMeals.isEmpty()) {
            prompt.append("(추천을 위한 데이터가 부족합니다. 일반적인 추천을 제공해주세요.)\n");
        } else {
            goodMeals.forEach(meal -> {
                prompt.append(String.format("- [%s] %s\n",
                        meal.getMealType().getDescription(),
                        meal.getMealName() != null ? meal.getMealName() : "식사"));

                if (meal.reaction() != null && meal.reaction().getOverallScore() != null) {
                    prompt.append(String.format("  · 반응 점수: %.1f/5.0\n",
                            meal.reaction().getOverallScore()));
                }

                if (meal.mealAnalysis() != null) {
                    MealAnalysis analysis = meal.mealAnalysis();
                    if (analysis.getCalories() != null) {
                        prompt.append(String.format("  · 칼로리: %.0fkcal\n", analysis.getCalories()));
                    }
                    if (analysis.getProtein() != null) {
                        prompt.append(String.format("  · 단백질: %.1fg\n", analysis.getProtein()));
                    }
                }
            });
        }

        prompt.append("\n");
    }

    private void appendResponseFormat(StringBuilder prompt) {
        prompt.append("## 추천 형식 (JSON)\n");
        prompt.append("아침, 점심, 저녁 각각에 대해 다음 정보를 포함한 JSON을 생성해주세요:\n\n");
        prompt.append("{\n");
        prompt.append("  \"breakfast\": {\n");
        prompt.append("    \"mealName\": \"추천 메뉴 이름\",\n");
        prompt.append("    \"reason\": \"추천 이유 (과거 반응 기반, 50자 이내)\",\n");
        prompt.append("    \"nutritionTips\": \"영양 팁 (30자 이내)\"\n");
        prompt.append("  },\n");
        prompt.append("  \"lunch\": {\n");
        prompt.append("    \"mealName\": \"추천 메뉴 이름\",\n");
        prompt.append("    \"reason\": \"추천 이유 (과거 반응 기반, 50자 이내)\",\n");
        prompt.append("    \"nutritionTips\": \"영양 팁 (30자 이내)\"\n");
        prompt.append("  },\n");
        prompt.append("  \"dinner\": {\n");
        prompt.append("    \"mealName\": \"추천 메뉴 이름\",\n");
        prompt.append("    \"reason\": \"추천 이유 (과거 반응 기반, 50자 이내)\",\n");
        prompt.append("    \"nutritionTips\": \"영양 팁 (30자 이내)\"\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        prompt.append("JSON 형식으로만 응답해주세요. 다른 설명 없이 JSON 데이터만 반환해주세요.\n");
    }

    @Builder
    public record MealWithReaction(
            Meal meal,
            MealAnalysis mealAnalysis,
            Reaction reaction
    ) {
        public String getMealName() {
            return mealAnalysis != null ? mealAnalysis.getMealName() : null;
        }

        public MealType getMealType() {
            return meal.getMealType();
        }
    }
}
