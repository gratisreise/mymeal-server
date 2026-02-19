package com.mymealserver.api.profile.service;

import com.mymealserver.entity.FoodMemberStats;
import com.mymealserver.repository.FoodMemberStatsRepository;
import com.mymealserver.repository.ReactionRepository;
import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.dto.response.BodyPatternTagResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodyPatternService {

    private final FoodMemberStatsRepository foodMemberStatsRepository;
    private final ReactionRepository reactionRepository;

    // Thresholds for classification
    private static final double GOOD_TAG_MIN_SCORE = 4.0;
    private static final double BAD_TAG_MAX_SCORE = 2.5;
    private static final int MIN_MEAL_COUNT = 3;

    /**
     * Analyze body patterns based on food statistics
     * Returns good tags (avg >= 4.0, min 3 meals) and bad tags (avg < 2.5, min 3 meals)
     */
    public BodyPatternResponse getBodyPatterns(Long memberId) {
        log.debug("Getting body patterns for member: {}", memberId);

        // Get all food stats for the member
        List<FoodMemberStats> foodStats = foodMemberStatsRepository.findByMemberId(memberId);

        if (foodStats.isEmpty()) {
            return new BodyPatternResponse(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0.0
            );
        }

        // Get overall average score from reactions
        Double overallAverage = reactionRepository.calculateAverageScoreByMemberId(memberId);
        if (overallAverage == null) {
            overallAverage = 0.0;
        }

        // Get tag statistics from FoodMemberStats
        List<Object[]> tagStats = foodMemberStatsRepository.findTagStatistics(memberId);

        // Classify tags into good and bad
        List<BodyPatternTagResponse> goodTags = new ArrayList<>();
        List<BodyPatternTagResponse> badTags = new ArrayList<>();

        for (Object[] row : tagStats) {
            String tagName = (String) row[1];
            double averageScore = ((Number) row[2]).doubleValue();
            int count = ((Number) row[3]).intValue();

            BodyPatternTagResponse tag = new BodyPatternTagResponse(tagName, averageScore, count);

            // Good tags: avg >= 4.0, min 3 meals
            if (averageScore >= GOOD_TAG_MIN_SCORE && count >= MIN_MEAL_COUNT) {
                goodTags.add(tag);
            }

            // Bad tags: avg < 2.5, min 3 meals
            if (averageScore < BAD_TAG_MAX_SCORE && count >= MIN_MEAL_COUNT) {
                badTags.add(tag);
            }
        }

        // Sort and limit to top 3
        goodTags.sort(Comparator.comparing(BodyPatternTagResponse::averageScore).reversed());
        badTags.sort(Comparator.comparing(BodyPatternTagResponse::averageScore));

        List<BodyPatternTagResponse> topGoodTags = goodTags.stream()
                .limit(3)
                .toList();

        List<BodyPatternTagResponse> topBadTags = badTags.stream()
                .limit(3)
                .toList();

        return new BodyPatternResponse(
                topGoodTags,
                topBadTags,
                Math.round(overallAverage * 100.0) / 100.0
        );
    }
}
