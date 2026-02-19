package com.mymealserver.api.recommendation.service;

import com.mymealserver.api.recommendation.dto.response.RecommendationResponse;
import com.mymealserver.api.recommendation.dto.response.RecommendationScheduleResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.MealAnalysisReader;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.member.MemberSettingsReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final MealReader mealReader;
    private final ReactionReader reactionReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final MemberSettingsReader memberSettingsReader;

    private static final int DEFAULT_LIMIT = 3;
    private static final int RECOMMENDATION_DAYS = 30;

    /**
     * Get personalized meal recommendations
     * Based on past meals with good reactions
     */
    public List<RecommendationResponse> getRecommendations(Long memberId, MealType mealType, Integer limit) {
        log.debug("Getting recommendations for memberId: {}, mealType: {}, limit: {}",
                memberId, mealType, limit);

        int recommendationLimit = (limit != null && limit > 0 && limit <= 10)
                ? limit
                : DEFAULT_LIMIT;

        // Calculate date range for past meals
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(RECOMMENDATION_DAYS);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Fetch past meals with same meal type
        List<Meal> meals = mealReader.findByMemberIdAndDateRange(memberId, startDateTime, endDateTime);

        // Filter by meal type if specified
        List<Meal> filteredMeals = mealType != null
                ? meals.stream()
                    .filter(meal -> meal.getMealType() == mealType)
                    .toList()
                : meals;

        if (filteredMeals.isEmpty()) {
            log.debug("No meals found for recommendation");
            return List.of();
        }

        // Get reactions for these meals
        List<Long> mealIds = filteredMeals.stream()
                .map(Meal::getId)
                .toList();

        Map<Long, Reaction> reactionMap = reactionReader.findByMealIdsAsMap(mealIds);

        // Get meal analyses for meal names
        Map<Long, MealAnalysis> analysisMap = mealIds.stream()
                .map(mealAnalysisReader::findByMealId)
                .filter(Optional::isPresent)
                .collect(Collectors.toMap(
                        analysis -> analysis.get().getMealId(),
                        analysis -> analysis.get()
                ));

        // Filter meals with GOOD reactions and build recommendations
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
                .limit(recommendationLimit)
                .toList();

        log.info("Generated {} recommendations for memberId: {}", recommendations.size(), memberId);

        // Convert to response
        return recommendations.stream()
                .map(item -> new RecommendationResponse(
                        item.mealName(),
                        item.reason(),
                        List.of(), // Empty tags list for now - full AI implementation deferred
                        item.averageScore(),
                        item.referenceMealId()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get recommendation schedule for push notifications
     * Returns scheduled recommendations based on user's meal time settings
     */
    public List<RecommendationScheduleResponse> getRecommendationSchedule(Long memberId) {
        log.debug("Getting recommendation schedule for memberId: {}", memberId);

        MemberSettings settings = memberSettingsReader.findByMemberIdOrNull(memberId);

        if (settings == null) {
            log.debug("No settings found for memberId: {}", memberId);
            return List.of();
        }

        List<RecommendationScheduleResponse> schedules = new ArrayList<>();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        // Schedule for each enabled meal reminder
        if (Boolean.TRUE.equals(settings.getMealReminderEnabled())) {
            // Breakfast
            if (settings.getBreakfastTime() != null) {
                LocalDateTime breakfastTime = today.with(settings.getBreakfastTime());
                RecommendationResponse breakfastRec = getSingleRecommendation(
                        memberId, MealType.BREAKFAST);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.BREAKFAST,
                        breakfastTime,
                        breakfastRec != null ? breakfastRec.mealName() : null,
                        breakfastRec != null ? breakfastRec.reason() : "건강한 아침 식사를 추천합니다"
                ));
            }

            // Lunch
            if (settings.getLunchTime() != null) {
                LocalDateTime lunchTime = today.with(settings.getLunchTime());
                RecommendationResponse lunchRec = getSingleRecommendation(
                        memberId, MealType.LUNCH);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.LUNCH,
                        lunchTime,
                        lunchRec != null ? lunchRec.mealName() : null,
                        lunchRec != null ? lunchRec.reason() : "점심 식사를 추천합니다"
                ));
            }

            // Dinner
            if (settings.getDinnerTime() != null) {
                LocalDateTime dinnerTime = today.with(settings.getDinnerTime());
                RecommendationResponse dinnerRec = getSingleRecommendation(
                        memberId, MealType.DINNER);

                schedules.add(new RecommendationScheduleResponse(
                        MealType.DINNER,
                        dinnerTime,
                        dinnerRec != null ? dinnerRec.mealName() : null,
                        dinnerRec != null ? dinnerRec.reason() : "저녁 식사를 추천합니다"
                ));
            }
        }

        log.debug("Generated {} recommendation schedules for memberId: {}", schedules.size(), memberId);
        return schedules;
    }

    /**
     * Get a single recommendation for a specific meal type
     * Returns null if no recommendation available
     */
    private RecommendationResponse getSingleRecommendation(Long memberId, MealType mealType) {
        List<RecommendationResponse> recommendations = getRecommendations(memberId, mealType, 1);
        return recommendations.isEmpty() ? null : recommendations.get(0);
    }

    /**
     * Internal record for recommendation items
     */
    private record RecommendationItem(
            String mealName,
            String reason,
            Double averageScore,
            Long referenceMealId
    ) {}
}
