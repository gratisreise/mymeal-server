package com.mymealserver.profile.service;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.profile.dto.response.StatisticsResponse;
import com.mymealserver.profile.dto.response.TagCountResponse;
import com.mymealserver.profile.dto.response.WeeklyTrendResponse;
import com.mymealserver.repository.FoodMemberStatsRepository;
import com.mymealserver.repository.MealRepository;
import com.mymealserver.repository.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 단위 테스트")
class StatisticsServiceTest {

    @Mock
    private MealRepository mealRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private FoodMemberStatsRepository foodMemberStatsRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Long testMemberId;
    private List<Meal> testMeals;
    private List<Object[]> mockTagStats;
    private List<Object[]> mockWeeklyTrend;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;

        // Create test meals
        testMeals = List.of(
                createMeal(1L, LocalDateTime.of(2025, 2, 15, 8, 0)),
                createMeal(2L, LocalDateTime.of(2025, 2, 15, 12, 30)),
                createMeal(3L, LocalDateTime.of(2025, 2, 16, 19, 0))
        );

        // Mock tag statistics: [foodId, tagName, avgScore, mealCount]
        mockTagStats = List.<Object[]>of(
                new Object[]{1L, "Chicken", 4.5, 5},
                new Object[]{2L, "Rice", 4.2, 4},
                new Object[]{3L, "Soup", 3.8, 3},
                new Object[]{4L, "Noodle", 3.5, 2},
                new Object[]{5L, "Salad", 4.0, 6}
        );

        // Mock weekly trend: [date, averageScore]
        mockWeeklyTrend = List.<Object[]>of(
                new Object[]{LocalDate.of(2025, 2, 10), 4.2},
                new Object[]{LocalDate.of(2025, 2, 11), 3.8},
                new Object[]{LocalDate.of(2025, 2, 12), 4.5},
                new Object[]{LocalDate.of(2025, 2, 13), 3.9},
                new Object[]{LocalDate.of(2025, 2, 14), 4.1},
                new Object[]{LocalDate.of(2025, 2, 15), 4.0},
                new Object[]{LocalDate.of(2025, 2, 16), 3.7}
        );
    }

    private Meal createMeal(Long id, LocalDateTime mealTime) {
        return Meal.builder()
                .id(id)
                .memberId(testMemberId)
                .mealType(MealType.LUNCH)
                .mealTime(mealTime)
                .photoUrl("https://example.com/meal.jpg")
                .photoKey("meal/meal.jpg")
                .build();
    }

    @Nested
    @DisplayName("통계 조회")
    class GetStatisticsTests {

        @Test
        @DisplayName("정상적인 데이터로 통계 조회에 성공한다")
        void getStatistics_WithValidData_ShouldReturnStatistics() {
            // Given
            int totalMealCount = 10;
            long reactionCount = 7;

            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn((long) totalMealCount);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(reactionCount);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(mockWeeklyTrend);

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalMealCount()).isEqualTo(totalMealCount);
            assertThat(response.reactionRate())
                    .isCloseTo(70.0, within(0.01)); // (7 / 10) * 100 = 70.0
            assertThat(response.topTags()).hasSize(5);
            assertThat(response.weeklyTrend()).hasSize(7);

            // Verify top tags
            assertThat(response.topTags().get(0).tag()).isEqualTo("Chicken");
            assertThat(response.topTags().get(0).count()).isEqualTo(5);

            // Verify weekly trend dates
            assertThat(response.weeklyTrend().get(0).date())
                    .isEqualTo(LocalDate.of(2025, 2, 10));
            assertThat(response.weeklyTrend().get(0).averageScore())
                    .isCloseTo(4.2, within(0.01));
        }

        @Test
        @DisplayName("식사 기록이 없는 회원(신규 사용자)의 통계 조회에 성공한다")
        void getStatistics_WithNoMeals_ShouldReturnZeroStatistics() {
            // Given
            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(0L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(List.of());
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(List.of());

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalMealCount()).isEqualTo(0);
            assertThat(response.reactionRate()).isEqualTo(0.0);
            assertThat(response.topTags()).isEmpty();
            assertThat(response.weeklyTrend()).isEmpty();
        }

        @Test
        @DisplayName("식사는 있으나 반응 없는 경우 통계 조회에 성공한다")
        void getStatistics_WithMealsButNoReactions_ShouldReturnZeroReactionRate() {
            // Given
            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(10L);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(0L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(mockWeeklyTrend);

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalMealCount()).isEqualTo(10);
            assertThat(response.reactionRate()).isEqualTo(0.0);
            assertThat(response.topTags()).hasSize(5);
            assertThat(response.weeklyTrend()).hasSize(7);
        }

        @Test
        @DisplayName("태그 통계가 없는 경우 통계 조회에 성공한다")
        void getStatistics_WithNoTagStats_ShouldReturnEmptyTopTags() {
            // Given
            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(10L);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(7L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(List.of());
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(mockWeeklyTrend);

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.topTags()).isEmpty();
            assertThat(response.weeklyTrend()).hasSize(7);
            assertThat(response.reactionRate())
                    .isCloseTo(70.0, within(0.01));
        }

        @Test
        @DisplayName("태그 통계 조회 실패 시 graceful degradation로 빈 리스트를 반환한다")
        void getStatistics_WhenTagStatsFails_ShouldReturnEmptyTopTags() {
            // Given
            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(10L);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(7L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenThrow(new RuntimeException("Database error"));
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(mockWeeklyTrend);

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.topTags()).isEmpty();
            assertThat(response.weeklyTrend()).hasSize(7);
            assertThat(response.reactionRate())
                    .isCloseTo(70.0, within(0.01));
        }

        @Test
        @DisplayName("주간 추이 조회 실패 시 graceful degradation로 빈 리스트를 반환한다")
        void getStatistics_WhenWeeklyTrendFails_ShouldReturnEmptyWeeklyTrend() {
            // Given
            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(10L);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(7L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.topTags()).hasSize(5);
            assertThat(response.weeklyTrend()).isEmpty();
            assertThat(response.reactionRate())
                    .isCloseTo(70.0, within(0.01));
        }

        @Test
        @DisplayName("반응률 계산 정확성을 검증한다")
        void getStatistics_ReactionRateCalculation_ShouldBeAccurate() {
            // Given
            int totalMealCount = 20;
            long reactionCount = 15;

            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn((long) totalMealCount);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(reactionCount);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(List.of());
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(List.of());

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response.reactionRate())
                    .isCloseTo(75.0, within(0.01)); // (15 / 20) * 100 = 75.0
        }

        @Test
        @DisplayName("Top 5 태그 제한을 올바르게 적용한다")
        void getStatistics_ShouldLimitTopTagsTo5() {
            // Given
            List<Object[]> manyTagStats = List.<Object[]>of(
                    new Object[]{1L, "Tag1", 4.5, 5},
                    new Object[]{2L, "Tag2", 4.2, 4},
                    new Object[]{3L, "Tag3", 3.8, 3},
                    new Object[]{4L, "Tag4", 3.5, 2},
                    new Object[]{5L, "Tag5", 4.0, 6},
                    new Object[]{6L, "Tag6", 3.9, 7},
                    new Object[]{7L, "Tag7", 4.1, 3}
            );

            when(mealRepository.countByMemberIdAndDeletedAtIsNull(testMemberId))
                    .thenReturn(10L);
            when(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                    eq(testMemberId), any(), any()))
                    .thenReturn(testMeals);
            when(reactionRepository.countByMealIdIn(any()))
                    .thenReturn(7L);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(manyTagStats);
            when(mealRepository.findWeeklyTrend(eq(testMemberId), any()))
                    .thenReturn(List.of());

            // When
            StatisticsResponse response = statisticsService.getStatistics(testMemberId);

            // Then
            assertThat(response.topTags()).hasSize(5);
        }
    }
}
