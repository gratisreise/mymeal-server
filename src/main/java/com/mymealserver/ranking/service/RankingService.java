package com.mymealserver.ranking.service;

import com.mymealserver.common.response.PageResponse;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.ranking.dto.response.RankingItemResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final MealReader mealReader;
    private final ReactionReader reactionReader;

    /**
     * Get best meals ranking (highest scores first)
     */
    public PageResponse<RankingItemResponse> getBestRanking(
            Long memberId,
            MealType mealType,
            DateRange dateRange,
            Pageable pageable
    ) {
        log.debug("Getting best ranking for member: {}, mealType: {}, dateRange: {}",
                memberId, mealType, dateRange);

        // Fetch meals with filters
        LocalDate startDate = dateRange != null ? dateRange.getStartDate() : null;
        LocalDate endDate = dateRange != null ? dateRange.getEndDate() : null;

        Page<Meal> mealPage = mealReader.findByMemberId(memberId, startDate, endDate, mealType, pageable);

        if (mealPage.isEmpty()) {
            return PageResponse.from(new PageImpl<>(List.of(), pageable, 0));
        }

        // Fetch reactions for these meals
        List<Long> mealIds = mealPage.getContent().stream()
                .map(Meal::getId)
                .toList();

        Map<Long, Reaction> reactionMap = reactionReader.findByMealIdsAsMap(mealIds);

        // Build ranking items
        List<RankingItem> rankingItems = buildRankingItems(mealPage.getContent(), reactionMap);

        // Sort by score DESC, then by mealTime DESC for tie-breaking
        rankingItems.sort((a, b) -> {
            int scoreCompare = Double.compare(b.getScore(), a.getScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return b.getMealTime().compareTo(a.getMealTime());
        });

        // Assign ranks
        assignRanks(rankingItems);

        // Convert to response and create page
        List<RankingItemResponse> responses = rankingItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.from(new PageImpl<>(responses, pageable, mealPage.getTotalElements()));
    }

    /**
     * Get worst meals ranking (lowest scores first)
     */
    public PageResponse<RankingItemResponse> getWorstRanking(
            Long memberId,
            MealType mealType,
            DateRange dateRange,
            Pageable pageable
    ) {
        log.debug("Getting worst ranking for member: {}, mealType: {}, dateRange: {}",
                memberId, mealType, dateRange);

        // Fetch meals with filters
        LocalDate startDate = dateRange != null ? dateRange.getStartDate() : null;
        LocalDate endDate = dateRange != null ? dateRange.getEndDate() : null;

        Page<Meal> mealPage = mealReader.findByMemberId(memberId, startDate, endDate, mealType, pageable);

        if (mealPage.isEmpty()) {
            return PageResponse.from(new PageImpl<>(List.of(), pageable, 0));
        }

        // Fetch reactions for these meals
        List<Long> mealIds = mealPage.getContent().stream()
                .map(Meal::getId)
                .toList();

        Map<Long, Reaction> reactionMap = reactionReader.findByMealIdsAsMap(mealIds);

        // Build ranking items
        List<RankingItem> rankingItems = buildRankingItems(mealPage.getContent(), reactionMap);

        // Sort by score ASC, then by mealTime DESC for tie-breaking
        rankingItems.sort((a, b) -> {
            int scoreCompare = Double.compare(a.getScore(), b.getScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return b.getMealTime().compareTo(a.getMealTime());
        });

        // Assign ranks
        assignRanks(rankingItems);

        // Convert to response and create page
        List<RankingItemResponse> responses = rankingItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.from(new PageImpl<>(responses, pageable, mealPage.getTotalElements()));
    }

    /**
     * Build ranking items from meals and reactions
     * Excludes meals without reactions
     */
    private List<RankingItem> buildRankingItems(List<Meal> meals, Map<Long, Reaction> reactionMap) {
        return meals.stream()
                .filter(meal -> reactionMap.containsKey(meal.getId()))
                .map(meal -> {
                    Reaction reaction = reactionMap.get(meal.getId());
                    return new RankingItem(
                            meal.getId(),
                            meal.getMemo(),
                            meal.getPhotoUrl(),
                            meal.getMealTime(),
                            meal.getMealType(),
                            reaction.getOverallScore(),
                            reaction.getGrade()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Assign ranks to sorted items
     * Items with same score get same rank, next rank skips appropriately
     */
    private void assignRanks(List<RankingItem> items) {
        if (items.isEmpty()) {
            return;
        }

        int rank = 1;
        items.get(0).setRank(rank);

        for (int i = 1; i < items.size(); i++) {
            RankingItem current = items.get(i);
            RankingItem previous = items.get(i - 1);

            // Same score as previous? Same rank
            if (Double.compare(current.getScore(), previous.getScore()) == 0) {
                current.setRank(previous.getRank());
            } else {
                current.setRank(i + 1);
            }
        }
    }

    /**
     * Convert RankingItem to RankingItemResponse
     */
    private RankingItemResponse toResponse(RankingItem item) {
        return new RankingItemResponse(
                item.getRank(),
                item.getMealId(),
                item.getMealName(),
                item.getPhotoUrl(),
                item.getMealTime(),
                item.getScore(),
                item.getGrade(),
                item.getMealType()
        );
    }

    /**
     * Internal mutable ranking item class
     */
    @Data
    private static class RankingItem {
        private final Long mealId;
        private final String mealName;
        private final String photoUrl;
        private final LocalDateTime mealTime;
        private final MealType mealType;
        private final Double score;
        private final GradeType grade;
        private Integer rank;
    }
}
