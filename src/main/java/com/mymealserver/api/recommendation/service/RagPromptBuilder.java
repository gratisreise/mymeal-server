package com.mymealserver.api.recommendation.service;

import com.mymealserver.domain.meallog.MealLog;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RagPromptBuilder {

  // 추천 프롬프트 템플릿
  private static final String RECOMMENDATION_PROMPT_TEMPLATE =
      """
        당신은 한국인의 식습관에 정통한 영양사입니다.
        사용자의 과거 긍정적인 식사 경험을 분석하여 오늘의 아침, 점심, 저녁 식사를 추천하세요.

        [과거 식사 데이터 - 긍정 반응 상위 3건]
        아래 데이터는 사용자가 먹은 음식과 당시 반응 요약입니다.
        %s

        [추천 기준]
        - 사용자가 맛, 포만감, 컨디션 측면에서 긍정적으로 반응한 음식을 우선 참고하세요.
        - 아침: 가볍고 소화가 잘 되는 식사를 추천하세요.
        - 점심: 활동 에너지를 위해 탄수화물과 단백질이 균형 잡힌 식사를 추천하세요.
        - 저녁: 과식을 피하고 소화 부담이 적은 식사를 추천하세요.
        - 세 끼가 서로 겹치지 않도록 다양하게 구성하세요.
        - 과거 데이터가 불충분한 경우, 한국인의 일반적인 식단을 기반으로 추천하세요.

        [응답 형식]
        반드시 아래 JSON 형식만 출력하세요. 설명, 마크다운, 코드블록은 절대 포함하지 마세요.
        모든 텍스트 필드는 한국어로 작성하세요.

        {
          "breakfast": {
            "mealName": "추천 음식명",
            "reason": "과거 반응 기반 추천 이유 (25자 이내)",
            "nutritionTips": "이 식사의 영양 포인트 (20자 이내)"
          },
          "lunch": {
            "mealName": "추천 음식명",
            "reason": "과거 반응 기반 추천 이유 (25자 이내)",
            "nutritionTips": "이 식사의 영양 포인트 (20자 이내)"
          },
          "dinner": {
            "mealName": "추천 음식명",
            "reason": "과거 반응 기반 추천 이유 (25자 이내)",
            "nutritionTips": "이 식사의 영양 포인트 (20자 이내)"
          }
        }
        """;


  // 유틸리티 클래스 - 인스턴스화 방지
  private RagPromptBuilder() {}
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
