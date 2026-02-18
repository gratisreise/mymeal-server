package com.mymealserver.calendar.service;

import com.mymealserver.calendar.domain.CalendarDataAggregator;
import com.mymealserver.calendar.domain.CalendarReader;
import com.mymealserver.calendar.dto.CalendarDailyResponse;
import com.mymealserver.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.calendar.dto.DailySummaryResponse;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.meal.dto.response.MealDetailResponse;
import com.mymealserver.reaction.dto.response.ReactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Calendar Service
 * 캘린더 관련 비즈니스 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarReader calendarReader;
    private final CalendarDataAggregator dataAggregator;

    /**
     * 월간 캘린더를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param year 년도
     * @param month 월
     * @return 월간 캘린더 응답
     */
    public CalendarMonthlyResponse getMonthlyCalendar(Long memberId, Integer year, Integer month) {
        log.info("Getting monthly calendar for member: {}, year: {}, month: {}", memberId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Fetch data
        List<Meal> meals = calendarReader.findMealsByDateRange(memberId, startOfMonth, endOfMonth);
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        Map<Long, Reaction> reactionsByMealId = calendarReader.findReactionsByMealIds(mealIds);
        Map<LocalDate, List<Meal>> mealsByDate = calendarReader.groupMealsByDate(meals);

        // Build daily summaries
        Map<String, DailySummaryResponse> days = new LinkedHashMap<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);
            List<Meal> dayMeals = mealsByDate.getOrDefault(date, List.of());

            if (!dayMeals.isEmpty()) {
                DailySummaryResponse summary = buildDailySummary(dayMeals, reactionsByMealId);
                days.put(date.toString(), summary);
            }
        }

        return new CalendarMonthlyResponse(year, month, days);
    }

    /**
     * 일별 캘린더를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param date 날짜
     * @return 일별 캘린더 응답
     */
    public CalendarDailyResponse getDailyCalendar(Long memberId, LocalDate date) {
        log.info("Getting daily calendar for member: {}, date: {}", memberId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Meal> meals = calendarReader.findMealsByDateRange(memberId, startOfDay, endOfDay);
        List<Long> mealIds = meals.stream().map(Meal::getId).toList();
        Map<Long, Reaction> reactionsByMealId = calendarReader.findReactionsByMealIds(mealIds);

        List<MealDetailResponse> mealResponses = meals.stream()
                .map(meal -> buildMealDetailResponse(meal, reactionsByMealId))
                .toList();

        CalendarDailyResponse.StatisticsResponse statistics = calculateStatistics(meals, reactionsByMealId);

        return new CalendarDailyResponse(date, statistics, mealResponses);
    }

    /**
     * 식사 상세 응답을 빌드합니다.
     *
     * @param meal 식사 기록
     * @param reactionsByMealId 식사 ID별 반응 맵
     * @return 식사 상세 응답
     */
    private MealDetailResponse buildMealDetailResponse(Meal meal, Map<Long, Reaction> reactionsByMealId) {
        Reaction reaction = reactionsByMealId.get(meal.getId());
        boolean hasReaction = reaction != null;

        // AI 분석 기능이 아직 구현되지 않음 - null 전달
        return MealDetailResponse.from(
                meal,
                null,
                reaction != null ? ReactionResponse.from(reaction) : null,
                hasReaction
        );
    }

    /**
     * 일별 요약 응답을 빌드합니다.
     *
     * @param dayMeals 해당 날짜의 식사 기록 목록
     * @param reactionsByMealId 식사 ID별 반응 맵
     * @return 일별 요약 응답
     */
    private DailySummaryResponse buildDailySummary(List<Meal> dayMeals, Map<Long, Reaction> reactionsByMealId) {
        List<Reaction> reactions = dataAggregator.filterValidReactions(dayMeals, reactionsByMealId);
        Double averageScore = dataAggregator.calculateAverageScore(reactions);
        GradeType quality = dataAggregator.classifyQuality(averageScore);
        Set<MealType> mealTypes = dataAggregator.extractMealTypes(dayMeals);

        return new DailySummaryResponse(
                dayMeals.size(),
                new ArrayList<>(mealTypes),
                averageScore,
                quality
        );
    }

    /**
     * 통계 정보를 계산합니다.
     *
     * @param meals 식사 기록 목록
     * @param reactionsByMealId 식사 ID별 반응 맵
     * @return 통계 응답
     */
    private CalendarDailyResponse.StatisticsResponse calculateStatistics(
            List<Meal> meals,
            Map<Long, Reaction> reactionsByMealId
    ) {
        List<Reaction> reactions = dataAggregator.filterValidReactions(meals, reactionsByMealId);
        Double averageScore = dataAggregator.calculateAverageScore(reactions);
        Double reactionRate = dataAggregator.calculateReactionRate(meals.size(), reactions.size());

        return new CalendarDailyResponse.StatisticsResponse(
                meals.size(),
                averageScore,
                reactionRate
        );
    }
}
