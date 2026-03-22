package com.mymealserver.api.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.api.recommendation.dto.response.RecommendationResponse;
import com.mymealserver.api.recommendation.dto.response.RecommendationScheduleResponse;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.domain.recommendation.RecommendationReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final MealReader mealReader;
    private final ReactionReader reactionReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final MemberSettingsReader memberSettingsReader;
    private final RecommendationReader recommendationReader;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_LIMIT = 3;
    private static final int RECOMMENDATION_DAYS = 30;


    public List<RecommendationResponse> getRecommendations(Long memberId, MealType mealType, Integer limit) {
        log.debug("Getting recommendations for memberId: {}, mealType: {}, limit: {}",
                memberId, mealType, limit);

        int recommendationLimit = (limit != null && limit > 0 && limit <= 10)
                ? limit
                : DEFAULT_LIMIT;

        // Try to get batch-generated recommendations first
        List<Recommendation> batchRecommendations = mealType != null
                ? recommendationReader.findByMemberIdAndMealType(memberId, mealType)
                : recommendationReader.findByMemberId(memberId);

        // Filter for today's recommendations only
        LocalDate today = LocalDate.now();
        List<Recommendation> todayRecommendations = batchRecommendations.stream()
                .filter(rec -> {
                    LocalDate recDate = rec.getScheduledTime().toLocalDate();
                    return recDate.equals(today) || !rec.isNotificationSent();
                })
                .limit(recommendationLimit)
                .toList();

        if (!todayRecommendations.isEmpty()) {
            log.info("Found {} batch-generated recommendations for memberId: {}",
                    todayRecommendations.size(), memberId);
            return todayRecommendations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }

        // Fallback to rule-based recommendations
        log.debug("No batch recommendations found, using rule-based fallback");
        return getRuleBasedRecommendations(memberId, mealType, recommendationLimit);
    }

    public List<RecommendationScheduleResponse> getRecommendationSchedule(Long memberId) {
        log.debug("Getting recommendation schedule for memberId: {}", memberId);

        MemberSettings settings = memberSettingsReader.findByMemberIdOrNull(memberId);

        if (settings == null) {
            log.debug("No settings found for memberId: {}", memberId);
            return List.of();
        }

        // Get today's batch recommendations
        List<Recommendation> todayRecommendations = recommendationReader.findByMemberIdAndDateRange(
                memberId,
                LocalDate.now()
        );

        List<RecommendationScheduleResponse> schedules = new ArrayList<>();

        // Convert batch recommendations to schedule responses
        for (Recommendation rec : todayRecommendations) {
            schedules.add(new RecommendationScheduleResponse(
                    rec.getMealType(),
                    rec.getScheduledTime(),
                    extractMealName(rec.getMenuDetails()),
                    rec.getPushMessage()
            ));
        }

        if (schedules.isEmpty()) {
            log.debug("No batch recommendations found, using fallback schedule");
            return getFallbackSchedule(memberId, settings);
        }

        log.debug("Generated {} recommendation schedules for memberId: {}", schedules.size(), memberId);
        return schedules;
    }

    private RecommendationResponse convertToResponse(Recommendation recommendation) {
        String mealName = extractMealName(recommendation.getMenuDetails());
        String reason = extractReason(recommendation.getMenuDetails());

        return new RecommendationResponse(
                mealName,
                reason,
                List.of(),
                null,
                null
        );
    }

    private String extractMealName(String menuDetailsJson) {
        if (menuDetailsJson == null || menuDetailsJson.isBlank()) {
            return "AI 추천 식단";
        }

        try {
            JsonNode root = objectMapper.readTree(menuDetailsJson);
            return root.has("mealName") ? root.get("mealName").asText() : "AI 추천 식단";
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse menu details JSON: {}", menuDetailsJson);
            return "AI 추천 식단";
        }
    }

    private String extractReason(String menuDetailsJson) {
        if (menuDetailsJson == null || menuDetailsJson.isBlank()) {
            return "AI가 분석한 개인 맞춤 추천입니다";
        }

        try {
            JsonNode root = objectMapper.readTree(menuDetailsJson);
            return root.has("reason") ? root.get("reason").asText() : "AI가 분석한 개인 맞춤 추천입니다";
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse menu details JSON: {}", menuDetailsJson);
            return "AI가 분석한 개인 맞춤 추천입니다";
        }
    }

    private List<RecommendationResponse> getRuleBasedRecommendations(
            Long memberId,
            MealType mealType,
            int limit
    ) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(RECOMMENDATION_DAYS);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Meal> meals = mealReader.findByMemberIdAndDateRange(memberId, startDateTime, endDateTime);

        List<Meal> filteredMeals = mealType != null
                ? meals.stream()
                    .filter(meal -> meal.getMealType() == mealType)
                    .toList()
                : meals;

        if (filteredMeals.isEmpty()) {
            log.debug("No meals found for recommendation");
            return List.of();
        }

        List<Long> mealIds = filteredMeals.stream()
                .map(Meal::getId)
                .toList();

        Map<Long, Reaction> reactionMap = reactionReader.findByMealIdsAsMap(mealIds);

        Map<Long, MealAnalysis> analysisMap = mealIds.stream()
                .map(mealAnalysisReader::findByMealId)
                .filter(Optional::isPresent)
                .collect(Collectors.toMap(
                        analysis -> analysis.get().getMealId(),
                        analysis -> analysis.get()
                ));

        List<RecommendationItem> recommendations = filteredMeals.stream()
                .filter(meal -> {
                    Reaction reaction = reactionMap.get(meal.getId());
                    return reaction != null && reaction.getGrade() == GradeType.GOOD;
                })
                .map(meal -> {
                    Reaction reaction = reactionMap.get(meal.getId());
                    MealAnalysis analysis = analysisMap.get(meal.getId());

                    String mealName = analysis != null && analysis.getMealName() != null
                            ? analysis.getMealName()
                            : meal.getMealType().getDescription();

                    return new RecommendationItem(
                            mealName,
                            "과거 좋은 반응이었습니다",
                            reaction.getOverallScore(),
                            meal.getId()
                    );
                })
                .sorted((a, b) -> Double.compare(b.averageScore(), a.averageScore()))
                .limit(limit)
                .toList();

        log.info("Generated {} rule-based recommendations for memberId: {}", recommendations.size(), memberId);

        return recommendations.stream()
                .map(item -> new RecommendationResponse(
                        item.mealName(),
                        item.reason(),
                        List.of(),
                        item.averageScore(),
                        item.referenceMealId()
                ))
                .collect(Collectors.toList());
    }

    private List<RecommendationScheduleResponse> getFallbackSchedule(
            Long memberId,
            MemberSettings settings
    ) {
        List<RecommendationScheduleResponse> schedules = new ArrayList<>();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        if (Boolean.TRUE.equals(settings.getMealReminderEnabled())) {
            if (settings.getBreakfastTime() != null) {
                RecommendationResponse breakfastRec = getSingleRecommendation(
                        memberId, MealType.BREAKFAST);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.BREAKFAST,
                        today.with(settings.getBreakfastTime()),
                        breakfastRec != null ? breakfastRec.mealName() : null,
                        breakfastRec != null ? breakfastRec.reason() : "건강한 아침 식사를 추천합니다"
                ));
            }

            if (settings.getLunchTime() != null) {
                RecommendationResponse lunchRec = getSingleRecommendation(
                        memberId, MealType.LUNCH);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.LUNCH,
                        today.with(settings.getLunchTime()),
                        lunchRec != null ? lunchRec.mealName() : null,
                        lunchRec != null ? lunchRec.reason() : "점심 식사를 추천합니다"
                ));
            }

            if (settings.getDinnerTime() != null) {
                RecommendationResponse dinnerRec = getSingleRecommendation(
                        memberId, MealType.DINNER);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.DINNER,
                        today.with(settings.getDinnerTime()),
                        dinnerRec != null ? dinnerRec.mealName() : null,
                        dinnerRec != null ? dinnerRec.reason() : "저녁 식사를 추천합니다"
                ));
            }
        }

        return schedules;
    }

    private RecommendationResponse getSingleRecommendation(Long memberId, MealType mealType) {
        List<RecommendationResponse> recommendations = getRecommendations(memberId, mealType, 1);
        return recommendations.isEmpty() ? null : recommendations.get(0);
    }

    private record RecommendationItem(
            String mealName,
            String reason,
            Double averageScore,
            Long referenceMealId
    ) {}
}
