package com.mymealserver.calendar.domain;

import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calendar Domain Reader
 * 캘린더 관련 조회 로직을 담당하는 Domain Reader
 */
@Service
@RequiredArgsConstructor
public class CalendarReader {

    private final MealReader mealReader;
    private final ReactionReader reactionReader;

    /**
     * 회원의 날짜 범위 내 식사 기록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @param start 시작 일시
     * @param end 종료 일시
     * @return 식사 기록 목록
     */
    public List<Meal> findMealsByDateRange(Long memberId, LocalDateTime start, LocalDateTime end) {
        return mealReader.findByMemberIdAndDateRange(memberId, start, end);
    }

    /**
     * 식사 ID 목록에 해당하는 반응 정보를 조회합니다.
     *
     * @param mealIds 식사 ID 목록
     * @return 식사 ID별 반응 맵
     */
    public Map<Long, Reaction> findReactionsByMealIds(List<Long> mealIds) {
        if (mealIds.isEmpty()) {
            return Map.of();
        }
        List<Reaction> reactions = reactionReader.findAllByMealIds(mealIds);
        return reactions.stream()
                .collect(Collectors.toMap(Reaction::getMealId, r -> r));
    }

    /**
     * 식사 기록을 날짜별로 그룹화합니다.
     *
     * @param meals 식사 기록 목록
     *
     * @return 날짜별 식사 기록 맵
     */
    public Map<LocalDate, List<Meal>> groupMealsByDate(List<Meal> meals) {
        return meals.stream()
                .collect(Collectors.groupingBy(meal -> meal.getMealTime().toLocalDate()));
    }
}
