package com.mymealserver.profile.service;

import com.mymealserver.entity.Meal;
import com.mymealserver.repository.FoodMemberStatsRepository;
import com.mymealserver.repository.MealRepository;
import com.mymealserver.repository.ReactionRepository;
import com.mymealserver.profile.dto.response.StatisticsResponse;
import com.mymealserver.profile.dto.response.TagCountResponse;
import com.mymealserver.profile.dto.response.WeeklyTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final MealRepository mealRepository;
    private final ReactionRepository reactionRepository;
    private final FoodMemberStatsRepository foodMemberStatsRepository;

    /**
     * Get comprehensive statistics for a member
     */
    public StatisticsResponse getStatistics(Long memberId) {
        log.debug("Getting statistics for member: {}", memberId);

        // 1. Total meal count
        long totalMealCount = mealRepository.countByMemberIdAndDeletedAtIsNull(memberId);

        // 2. Reaction rate calculation
        Double reactionRate = calculateReactionRate(memberId, totalMealCount);

        // 3. Top tags (example implementation)
        List<TagCountResponse> topTags = getTopTags(memberId);

        // 4. Weekly trend (last 7 days)
        List<WeeklyTrendResponse> weeklyTrend = getWeeklyTrend(memberId);

        return new StatisticsResponse(
                (int) totalMealCount,
                reactionRate,
                topTags,
                weeklyTrend
        );
    }

    /**
     * Calculate reaction rate: (meals with reactions / total meals) * 100
     */
    private Double calculateReactionRate(Long memberId, long totalMealCount) {
        if (totalMealCount == 0) {
            return 0.0;
        }

        // Use the existing findByMemberIdAndDateRange method with a wide range
        List<Meal> meals = mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
            memberId,
            LocalDateTime.now().minusYears(100),
            LocalDateTime.now()
        );

        if (meals.isEmpty()) {
            return 0.0;
        }

        // Get meal IDs
        List<Long> mealIds = meals.stream()
                .map(Meal::getId)
                .toList();

        // Count reactions
        long reactionCount = reactionRepository.countByMealIdIn(mealIds);

        return (reactionCount * 100.0) / totalMealCount;
    }

    /**
     * Get top 5 most eaten tags
     *
     * TODO: This is a simplified example implementation.
     * The actual implementation should:
     * 1. Query MealAnalysis to get all Food IDs for the member
     * 2. Join with Food table to get tags
     * 3. Aggregate tag counts and sort by frequency
     * 4. Return top 5 tags
     *
     * For now, we return food names as a proxy for tags.
     */
    private List<TagCountResponse> getTopTags(Long memberId) {
        log.debug("Getting top tags for member: {}", memberId);

        try {
            // Get food statistics for the member
            List<Object[]> tagStats = foodMemberStatsRepository.findTagStatistics(memberId);

            // Convert to TagCountResponse and limit to top 5
            List<TagCountResponse> tags = tagStats.stream()
                    .limit(5)
                    .map(row -> new TagCountResponse(
                            (String) row[1], // tag name (using food name as proxy)
                            ((Number) row[3]).intValue() // meal count
                    ))
                    .toList();

            return tags;

        } catch (Exception e) {
            log.warn("Failed to get top tags for member: {}, returning empty list", memberId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get weekly trend - daily average scores for the last 7 days
     */
    private List<WeeklyTrendResponse> getWeeklyTrend(Long memberId) {
        log.debug("Getting weekly trend for member: {}", memberId);

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            List<Object[]> results = mealRepository.findWeeklyTrend(memberId, startDate);

            return results.stream()
                    .map(row -> new WeeklyTrendResponse(
                            (LocalDate) row[0], // date
                            ((Number) row[1]).doubleValue() // average score
                    ))
                    .toList();

        } catch (Exception e) {
            log.warn("Failed to get weekly trend for member: {}, returning empty list", memberId, e);
            return new ArrayList<>();
        }
    }
}
