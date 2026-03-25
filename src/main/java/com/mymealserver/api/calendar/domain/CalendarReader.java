package com.mymealserver.api.calendar.domain;

import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarReader {

    private final MealReader mealReader;
    private final ReactionReader reactionReader;

    public List<Meal> findMealsByDateRange(Long memberId, LocalDateTime start, LocalDateTime end) {
        return mealReader.findByMemberIdAndDateRange(memberId, start, end);
    }

    public Map<Long, Reaction> findReactionsByMealIds(List<Long> mealIds) {
        if (mealIds.isEmpty()) {
            return Map.of();
        }
        List<Reaction> reactions = reactionReader.findAllByMealIds(mealIds);
        return reactions.stream()
                .collect(Collectors.toMap(Reaction::getMealId, r -> r));
    }

    public Map<LocalDate, List<Meal>> groupMealsByDate(List<Meal> meals) {
        return meals.stream()
                .collect(Collectors.groupingBy(meal -> meal.getMealTime().toLocalDate()));
    }
}
