package com.mymealserver.api.calendar.service;

import com.mymealserver.api.calendar.dto.CalendarDailyResponse;
import com.mymealserver.api.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.api.calendar.dto.DailySummaryResponse;
import com.mymealserver.api.meal.dto.response.AIAnalysisResponse;
import com.mymealserver.api.meal.dto.response.MealDetailResponse;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.reaction.Reaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

  private final CalendarReader calendarReader;
  private final CalendarDataAggregator dataAggregator;

  public CalendarMonthlyResponse getMonthlyCalendar(Long memberId, Integer year, Integer month) {

    YearMonth yearMonth = YearMonth.of(year, month);
    LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

    List<Meal> meals = calendarReader.findMealsByDateRange(memberId, startOfMonth, endOfMonth);
    List<Long> mealIds = meals.stream().map(Meal::getId).toList();
    Map<Long, Reaction> reactionsByMealId = calendarReader.findReactionsByMealIds(mealIds);
    Map<LocalDate, List<Meal>> mealsByDate = calendarReader.groupMealsByDate(meals);
    Map<LocalDate, DailySummaryResponse> days = new LinkedHashMap<>();
    for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
      LocalDate date = LocalDate.of(year, month, day);
      List<Meal> dayMeals = mealsByDate.getOrDefault(date, List.of());

      if (!dayMeals.isEmpty()) {
        DailySummaryResponse summary = buildDailySummary(dayMeals, reactionsByMealId);
        days.put(date, summary);
      }
    }

    return new CalendarMonthlyResponse(year, month, days);
  }

  public CalendarDailyResponse getDailyCalendar(Long memberId, LocalDate date) {
    log.info("Getting daily calendar for member: {}, date: {}", memberId, date);

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    List<Meal> meals = calendarReader.findMealsByDateRange(memberId, startOfDay, endOfDay);
    List<Long> mealIds = meals.stream().map(Meal::getId).toList();
    Map<Long, Reaction> reactionsByMealId = calendarReader.findReactionsByMealIds(mealIds);
    Map<Long, MealAnalysis> analysesByMealId = calendarReader.findMealAnalysesByMealIds(mealIds);

    List<MealDetailResponse> mealResponses =
        meals.stream()
            .map(
                meal ->
                    buildMealDetailResponse(
                        meal,
                        reactionsByMealId.get(meal.getId()),
                        analysesByMealId.get(meal.getId())))
            .toList();

    CalendarDailyResponse.StatisticsResponse statistics =
        calculateStatistics(meals, reactionsByMealId);

    return new CalendarDailyResponse(date, statistics, mealResponses);
  }

  private MealDetailResponse buildMealDetailResponse(
      Meal meal, Reaction reaction, MealAnalysis analysis) {
    boolean hasReaction = reaction != null;

    AIAnalysisResponse aiAnalysis = analysis != null ? AIAnalysisResponse.from(analysis) : null;

    return MealDetailResponse.from(
        meal, aiAnalysis, hasReaction ? ReactionResponse.from(reaction) : null, hasReaction);
  }

  private DailySummaryResponse buildDailySummary(
      List<Meal> dayMeals, Map<Long, Reaction> reactionsByMealId) {
    List<Reaction> reactions = dataAggregator.filterValidReactions(dayMeals, reactionsByMealId);
    Double averageScore = dataAggregator.calculateAverageScore(reactions);
    GradeType quality = dataAggregator.classifyQuality(averageScore);
    Set<MealType> mealTypes = dataAggregator.extractMealTypes(dayMeals);
    Double reactionRate = dataAggregator.calculateReactionRate(dayMeals.size(), reactions.size());

    return new DailySummaryResponse(
        dayMeals.size(), new ArrayList<>(mealTypes), averageScore, quality, reactionRate);
  }

  private CalendarDailyResponse.StatisticsResponse calculateStatistics(
      List<Meal> meals, Map<Long, Reaction> reactionsByMealId) {
    List<Reaction> reactions = dataAggregator.filterValidReactions(meals, reactionsByMealId);
    Double averageScore = dataAggregator.calculateAverageScore(reactions);
    Double reactionRate = dataAggregator.calculateReactionRate(meals.size(), reactions.size());

    return new CalendarDailyResponse.StatisticsResponse(meals.size(), averageScore, reactionRate);
  }
}
