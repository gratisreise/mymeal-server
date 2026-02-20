package com.mymealserver.batch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.Recommendation;
import com.mymealserver.domain.member.MemberSettingsReader;
import com.mymealserver.service.recommendation.RagPromptBuilder;
import com.mymealserver.service.recommendation.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationProcessor implements ItemProcessor<Member, List<Recommendation>> {

    private final VectorSearchService vectorSearchService;
    private final AiAnalysisService aiAnalysisService;
    private final MemberSettingsReader memberSettingsReader;
    private final ObjectMapper objectMapper;

    @Override
    public List<Recommendation> process(Member member) throws Exception {
        log.info("Processing recommendations for memberId: {}", member.getId());

        try {
            // 1. Get member settings
            MemberSettings settings = memberSettingsReader.findByMemberIdOrNull(member.getId());

            if (settings == null) {
                log.warn("No settings found for member: {}, skipping", member.getId());
                return null;
            }

            // 2. Check if meal times are configured
            if (settings.getBreakfastTime() == null &&
                settings.getLunchTime() == null &&
                settings.getDinnerTime() == null) {
                log.warn("No meal times configured for member: {}, skipping", member.getId());
                return null;
            }

            // 3. Gather context using RAG
            List<RagPromptBuilder.MealWithReaction> goodMeals =
                    vectorSearchService.findRecentGoodMeals(member.getId(), 30);

            if (goodMeals.isEmpty()) {
                log.warn("Insufficient data for recommendation for member: {}, skipping", member.getId());
                return null;
            }

            // 4. Build RAG prompt
            String prompt = RagPromptBuilder.builder()
                    .member(member)
                    .goodMeals(goodMeals)
                    .build()
                    .buildRecommendationPrompt();

            // 5. Call AI to generate recommendations
            String aiResponse = generateRecommendations(prompt);

            // 6. Parse AI response
            JsonNode recommendationsJson = parseAiResponse(aiResponse);

            // 7. Create Recommendation entities
            List<Recommendation> recommendations = new ArrayList<>();
            LocalDateTime today = LocalDate.now().atStartOfDay();

            // Breakfast
            if (settings.getBreakfastTime() != null) {
                LocalDateTime breakfastTime = today.with(settings.getBreakfastTime());
                recommendations.add(createRecommendation(
                        member.getId(),
                        recommendationsJson.get("breakfast"),
                        com.mymealserver.entity.enums.MealType.BREAKFAST,
                        breakfastTime
                ));
            }

            // Lunch
            if (settings.getLunchTime() != null) {
                LocalDateTime lunchTime = today.with(settings.getLunchTime());
                recommendations.add(createRecommendation(
                        member.getId(),
                        recommendationsJson.get("lunch"),
                        com.mymealserver.entity.enums.MealType.LUNCH,
                        lunchTime
                ));
            }

            // Dinner
            if (settings.getDinnerTime() != null) {
                LocalDateTime dinnerTime = today.with(settings.getDinnerTime());
                recommendations.add(createRecommendation(
                        member.getId(),
                        recommendationsJson.get("dinner"),
                        com.mymealserver.entity.enums.MealType.DINNER,
                        dinnerTime
                ));
            }

            log.info("Generated {} recommendations for memberId: {}",
                    recommendations.size(), member.getId());

            return recommendations.isEmpty() ? null : recommendations;

        } catch (Exception e) {
            log.error("Error processing recommendations for member: {}", member.getId(), e);
            throw e;
        }
    }

    private String generateRecommendations(String prompt) {
        log.debug("Calling AI to generate recommendations");

        // TODO: Create a dedicated method in AiAnalysisService for text-based recommendations
        // For now, we'll need to add this method to AiAnalysisService

        // This is a placeholder - we'll need to implement the actual call
        // For now, return a mock response
        return """
                {
                  "breakfast": {
                    "mealName": "오트밀과 과일",
                    "reason": "과거 좋은 반응이었던 가벼운 아침 식사",
                    "nutritionTips": "복합 탄수화물과 비타민 공급"
                  },
                  "lunch": {
                    "mealName": "현미밥과 구운 연어",
                    "reason": "고단백 저지방 식단 선호도 반영",
                    "nutritionTips": "오메가-3와 단백질 풍부"
                  },
                  "dinner": {
                    "mealName": "닭가슴살 샐러드",
                    "reason": "소화 부담 없는 저녁 식사",
                    "nutritionTips": "가벼운 식사로 숙면 도움"
                  }
                }
                """;
    }

    private JsonNode parseAiResponse(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON", e);
            throw new BusinessException(ErrorCode.AI_ANALYSIS_ERROR);
        }
    }

    private Recommendation createRecommendation(
            Long memberId,
            JsonNode mealRec,
            com.mymealserver.entity.enums.MealType mealType,
            LocalDateTime scheduledTime
    ) {
        String mealName = mealRec.get("mealName").asText();
        String reason = mealRec.get("reason").asText();
        String nutritionTips = mealRec.get("nutritionTips").asText();

        // Build push message
        String pushMessage = String.format("🍽️ %s 추천: %s\n\n%s",
                mealType.getDescription(),
                mealName,
                reason
        );

        // Build menu details JSON
        String menuDetails = String.format(
                "{\"mealName\":\"%s\",\"reason\":\"%s\",\"nutritionTips\":\"%s\"}",
                mealName, reason, nutritionTips
        );

        return Recommendation.builder()
                .memberId(memberId)
                .mealType(mealType)
                .scheduledTime(scheduledTime)
                .menuDetails(menuDetails)
                .pushMessage(pushMessage)
                .isSent(false)
                .build();
    }
}
