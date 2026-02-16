package com.mymealserver.calendar.domain;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calendar Data Aggregator
 * 캘린더 관련 데이터 집계 로직을 담당하는 컴포넌트
 * 코드 중복을 방지하기 위해 식사-반응 집계 로직을 추출
 */
@Component
public class CalendarDataAggregator {

    /**
     * 식사 기록과 반응 정보를 집계하여 맵으로 변환합니다.
     *
     * @param meals 식사 기록 목록
     * @param reactionsByMealId 식사 ID별 반응 맵
     * @return 식사 ID별 반응 맵
     */
    public Map<Long, Reaction> aggregateReactions(List<Meal> meals, Map<Long, Reaction> reactionsByMealId) {
        return meals.stream()
                .collect(Collectors.toMap(Meal::getId, meal -> reactionsByMealId.get(meal.getId())));
    }

    /**
     * 유효한 반응만 필터링합니다.
     *
     * @param meals 식사 기록 목록
     * @param reactionsByMealId 식사 ID별 반응 맵
     * @return 유효한 반응 목록
     */
    public List<Reaction> filterValidReactions(List<Meal> meals, Map<Long, Reaction> reactionsByMealId) {
        return meals.stream()
                .map(meal -> reactionsByMealId.get(meal.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 반응의 평균 점수를 계산합니다.
     *
     * @param reactions 반응 목록
     * @return 평균 점수 (반응이 없으면 null)
     */
    public Double calculateAverageScore(List<Reaction> reactions) {
        if (reactions.isEmpty()) {
            return null;
        }
        return reactions.stream()
                .mapToDouble(Reaction::getOverallScore)
                .average()
                .orElse(0.0);
    }

    /**
     * 반응률을 계산합니다.
     *
     * @param mealCount 식사 횟수
     * @param reactionCount 반응 횟수
     * @return 반응률 (0-100)
     */
    public Double calculateReactionRate(int mealCount, int reactionCount) {
        return mealCount > 0 ? (reactionCount * 100.0 / mealCount) : 0.0;
    }

    /**
     * 평균 점수를 기준으로 식사 품질 등급을 분류합니다.
     *
     * @param averageScore 평균 점수
     * @return 품질 등급 (null: 반응 없음, GOOD: 4.0 이상, NORMAL: 2.5 이상, BAD: 2.5 미만)
     */
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

    /**
     * 식사 기록에서 식사 유형을 추출합니다.
     *
     * @param meals 식사 기록 목록
     * @return 중복 제거된 식사 유형 집합
     */
    public Set<MealType> extractMealTypes(List<Meal> meals) {
        return meals.stream()
                .map(Meal::getMealType)
                .collect(Collectors.toSet());
    }
}
