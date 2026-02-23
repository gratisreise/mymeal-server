package com.mymealserver.api.calendar.service;

import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CalendarDataAggregator {

    public Map<Long, Reaction> aggregateReactions(List<Meal> meals, Map<Long, Reaction> reactionsByMealId) {
        return meals.stream()
                .collect(Collectors.toMap(Meal::getId, meal -> reactionsByMealId.get(meal.getId())));
    }

    public List<Reaction> filterValidReactions(List<Meal> meals, Map<Long, Reaction> reactionsByMealId) {
        return meals.stream()
                .map(meal -> reactionsByMealId.get(meal.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Double calculateAverageScore(List<Reaction> reactions) {
        if (reactions.isEmpty()) {
            return null;
        }
        return reactions.stream()
                .mapToDouble(Reaction::getOverallScore)
                .average()
                .orElse(0.0);
    }

    public Double calculateReactionRate(int mealCount, int reactionCount) {
        return mealCount > 0 ? (reactionCount * 100.0 / mealCount) : 0.0;
    }

    public GradeType classifyQuality(Double averageScore) {
        if (averageScore == null) {
            return null;
        }
        if (averageScore >= 4.0) {
            return GradeType.GOOD;
        } else if (averageScore >= 2.5) {
            return GradeType.NORMAL;
        } else {
            return GradeType.BAD;
        }
    }

    public Set<MealType> extractMealTypes(List<Meal> meals) {
        return meals.stream()
                .map(Meal::getMealType)
                .collect(Collectors.toSet());
    }
}
