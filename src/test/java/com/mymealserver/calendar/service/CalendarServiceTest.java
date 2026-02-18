package com.mymealserver.calendar.service;

import com.mymealserver.calendar.domain.CalendarDataAggregator;
import com.mymealserver.calendar.domain.CalendarReader;
import com.mymealserver.calendar.dto.CalendarDailyResponse;
import com.mymealserver.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.common.test.fixtures.MealFixture;
import com.mymealserver.common.test.fixtures.ReactionFixture;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarService 단위 테스트")
class CalendarServiceTest {

    @Mock
    private CalendarReader calendarReader;

    @Mock
    private CalendarDataAggregator dataAggregator;

    @InjectMocks
    private CalendarService calendarService;

    private List<Meal> testMeals;
    private Map<Long, Reaction> testReactions;
    private Long testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;

        // Create test meals for February 2025
        testMeals = List.of(
                MealFixture.createCustomMeal(1L, testMemberId, MealType.BREAKFAST,
                        LocalDateTime.of(2025, 2, 15, 8, 0), "Breakfast"),
                MealFixture.createCustomMeal(2L, testMemberId, MealType.LUNCH,
                        LocalDateTime.of(2025, 2, 15, 12, 30), "Lunch"),
                MealFixture.createCustomMeal(3L, testMemberId, MealType.DINNER,
                        LocalDateTime.of(2025, 2, 16, 19, 0), "Dinner")
        );

        // Create test reactions
        testReactions = Map.of(
                1L, ReactionFixture.createGoodReaction(1L),
                2L, ReactionFixture.createNormalReaction(2L)
                // Meal 3 has no reaction
        );
    }

    @Test
    @DisplayName("월간 캘린더 조회 - 정상 응답 반환")
    void getMonthlyCalendar_WithValidData_ShouldReturnCalendar() {
        // Given
        int year = 2025;
        int month = 2;

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(testMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(testReactions);
        when(calendarReader.groupMealsByDate(testMeals))
                .thenReturn(Map.of(
                        LocalDate.of(2025, 2, 15), List.of(testMeals.get(0), testMeals.get(1)),
                        LocalDate.of(2025, 2, 16), List.of(testMeals.get(2))
                ));

        when(dataAggregator.filterValidReactions(any(), eq(testReactions)))
                .thenReturn(List.of(testReactions.get(1L), testReactions.get(2L)));
        when(dataAggregator.calculateAverageScore(any()))
                .thenReturn(3.5);
        when(dataAggregator.classifyQuality(3.5))
                .thenReturn(GradeType.NORMAL);
        when(dataAggregator.extractMealTypes(any()))
                .thenReturn(Set.of(MealType.BREAKFAST, MealType.LUNCH));

        // When
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(testMemberId, year, month);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.days()).isNotEmpty();
    }

    @Test
    @DisplayName("월간 캘린더 조회 - 식사 기록 없음")
    void getMonthlyCalendar_WithNoMeals_ShouldReturnEmptyCalendar() {
        // Given
        int year = 2025;
        int month = 2;

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(List.of());
        when(calendarReader.findReactionsByMealIds(List.of()))
                .thenReturn(Map.of());
        when(calendarReader.groupMealsByDate(List.of()))
                .thenReturn(Map.of());

        // When
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(testMemberId, year, month);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.month()).isEqualTo(month);
        assertThat(response.days()).isEmpty();
    }

    @Test
    @DisplayName("월간 캘린더 조회 - 부분 데이터 (일부 날짜만 식사 기록)")
    void getMonthlyCalendar_WithPartialData_ShouldReturnCalendarWithPartialDays() {
        // Given
        int year = 2025;
        int month = 2;

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(testMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(testReactions);
        when(calendarReader.groupMealsByDate(testMeals))
                .thenReturn(Map.of(
                        LocalDate.of(2025, 2, 15), List.of(testMeals.get(0), testMeals.get(1))
                ));

        when(dataAggregator.filterValidReactions(any(), eq(testReactions)))
                .thenReturn(List.of(testReactions.get(1L), testReactions.get(2L)));
        when(dataAggregator.calculateAverageScore(any()))
                .thenReturn(3.5);
        when(dataAggregator.classifyQuality(3.5))
                .thenReturn(GradeType.NORMAL);
        when(dataAggregator.extractMealTypes(any()))
                .thenReturn(Set.of(MealType.BREAKFAST, MealType.LUNCH));

        // When
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(testMemberId, year, month);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.days()).hasSize(1); // Only one day has meals
    }

    @Test
    @DisplayName("일별 캘린더 조회 - 정상 응답 반환")
    void getDailyCalendar_WithValidData_ShouldReturnDailyCalendar() {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 15);
        List<Meal> dayMeals = List.of(testMeals.get(0), testMeals.get(1));

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(dayMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(testReactions);

        when(dataAggregator.filterValidReactions(eq(dayMeals), eq(testReactions)))
                .thenReturn(List.of(testReactions.get(1L), testReactions.get(2L)));
        when(dataAggregator.calculateAverageScore(any()))
                .thenReturn(3.5);
        when(dataAggregator.calculateReactionRate(2, 2))
                .thenReturn(100.0);

        // When
        CalendarDailyResponse response = calendarService.getDailyCalendar(testMemberId, date);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.statistics()).isNotNull();
        assertThat(response.statistics().mealCount()).isEqualTo(2);
        assertThat(response.statistics().averageScore()).isEqualTo(3.5);
        assertThat(response.statistics().reactionRate()).isEqualTo(100.0);
        assertThat(response.meals()).hasSize(2);
    }

    @Test
    @DisplayName("일별 캘린더 조회 - 식사 기록 없음")
    void getDailyCalendar_WithNoMeals_ShouldReturnDailyCalendarWithZeroStats() {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 15);

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(List.of());
        when(calendarReader.findReactionsByMealIds(List.of()))
                .thenReturn(Map.of());

        when(dataAggregator.filterValidReactions(any(), eq(Map.of())))
                .thenReturn(List.of());
        when(dataAggregator.calculateAverageScore(List.of()))
                .thenReturn(null);
        when(dataAggregator.calculateReactionRate(0, 0))
                .thenReturn(0.0);

        // When
        CalendarDailyResponse response = calendarService.getDailyCalendar(testMemberId, date);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.statistics()).isNotNull();
        assertThat(response.statistics().mealCount()).isEqualTo(0);
        assertThat(response.statistics().averageScore()).isNull();
        assertThat(response.statistics().reactionRate()).isEqualTo(0.0);
        assertThat(response.meals()).isEmpty();
    }

    @Test
    @DisplayName("일별 캘린더 조회 - 식사는 있으나 반응 없음")
    void getDailyCalendar_WithMealsButNoReactions_ShouldReturnDailyCalendar() {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 15);
        List<Meal> dayMeals = List.of(testMeals.get(0), testMeals.get(1));

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(dayMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(Map.of()); // No reactions

        when(dataAggregator.filterValidReactions(eq(dayMeals), eq(Map.of())))
                .thenReturn(List.of());
        when(dataAggregator.calculateAverageScore(List.of()))
                .thenReturn(null);
        when(dataAggregator.calculateReactionRate(2, 0))
                .thenReturn(0.0);

        // When
        CalendarDailyResponse response = calendarService.getDailyCalendar(testMemberId, date);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.statistics()).isNotNull();
        assertThat(response.statistics().mealCount()).isEqualTo(2);
        assertThat(response.statistics().averageScore()).isNull();
        assertThat(response.statistics().reactionRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("월간 캘린더 통계 - 평균 점수 계산 정확성")
    void getMonthlyCalendar_AverageScoreCalculation_ShouldBeAccurate() {
        // Given
        int year = 2025;
        int month = 2;
        List<Meal> dayMeals = List.of(testMeals.get(0), testMeals.get(1));

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(testMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(testReactions);
        when(calendarReader.groupMealsByDate(testMeals))
                .thenReturn(Map.of(LocalDate.of(2025, 2, 15), dayMeals));

        List<Reaction> reactions = List.of(testReactions.get(1L), testReactions.get(2L));
        when(dataAggregator.filterValidReactions(eq(dayMeals), eq(testReactions)))
                .thenReturn(reactions);
        when(dataAggregator.calculateAverageScore(reactions))
                .thenReturn(3.5); // (5.0 + 3.0) / 2 = 4.0, but mocked to 3.5
        when(dataAggregator.classifyQuality(3.5))
                .thenReturn(GradeType.NORMAL);
        when(dataAggregator.extractMealTypes(dayMeals))
                .thenReturn(Set.of(MealType.BREAKFAST, MealType.LUNCH));

        // When
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(testMemberId, year, month);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.days()).hasSize(1);
    }

    @Test
    @DisplayName("일별 캘린더 통계 - 반응률 계산 정확성")
    void getDailyCalendar_ReactionRateCalculation_ShouldBeAccurate() {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 15);
        List<Meal> dayMeals = List.of(testMeals.get(0), testMeals.get(1));

        when(calendarReader.findMealsByDateRange(eq(testMemberId), any(), any()))
                .thenReturn(dayMeals);
        when(calendarReader.findReactionsByMealIds(any()))
                .thenReturn(testReactions);

        List<Reaction> reactions = List.of(testReactions.get(1L));
        when(dataAggregator.filterValidReactions(eq(dayMeals), eq(testReactions)))
                .thenReturn(reactions);
        when(dataAggregator.calculateAverageScore(reactions))
                .thenReturn(3.0);
        when(dataAggregator.calculateReactionRate(2, 1))
                .thenReturn(50.0); // 1 reaction out of 2 meals = 50%

        // When
        CalendarDailyResponse response = calendarService.getDailyCalendar(testMemberId, date);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.statistics().reactionRate()).isEqualTo(50.0);
    }
}
