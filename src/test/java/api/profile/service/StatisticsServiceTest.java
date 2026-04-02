package api.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.mymealserver.api.profile.dto.response.StatisticsResponse;
import com.mymealserver.api.profile.service.StatisticsService;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.foodmemberstats.FoodMemberStatsRepository;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealRepository;
import com.mymealserver.domain.reaction.ReactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

  @Mock private MealRepository mealRepository;
  @Mock private ReactionRepository reactionRepository;
  @Mock private FoodMemberStatsRepository foodMemberStatsRepository;

  @InjectMocks private StatisticsService statisticsService;

  private static final Long MEMBER_ID = 1L;

  // ========================
  // getStatistics
  // ========================

  @Test
  void getStatistics_success_noMeals() {
    // given
    given(mealRepository.countByMemberIdAndDeletedAtIsNull(MEMBER_ID)).willReturn(0L);

    // when
    StatisticsResponse response = statisticsService.getStatistics(MEMBER_ID);

    // then
    assertThat(response.totalMealCount()).isEqualTo(0);
    assertThat(response.reactionRate()).isEqualTo(0.0);
    assertThat(response.topTags()).isEmpty();
    assertThat(response.weeklyTrend()).isEmpty();
  }

  @Test
  void getStatistics_success_withMealsAndReactions() {
    // given
    given(mealRepository.countByMemberIdAndDeletedAtIsNull(MEMBER_ID)).willReturn(10L);

    List<Meal> meals = List.of(
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now()),
        createMeal(2L, MEMBER_ID, MealType.DINNER, LocalDateTime.now())
    );
    given(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(meals);

    given(reactionRepository.countByMealIdIn(List.of(1L, 2L))).willReturn(5L);

    // tag statistics
    List<Object[]> tagStats = List.of(
        new Object[]{MEMBER_ID, "비빔밥", 4.5, 8},
        new Object[]{MEMBER_ID, "된장찌개", 3.8, 5},
        new Object[]{MEMBER_ID, "김치찌개", 3.2, 3}
    );
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(tagStats);

    // weekly trend
    List<Object[]> weeklyTrend = List.of(
        new Object[]{LocalDate.of(2026, 4, 1), 4.2},
        new Object[]{LocalDate.of(2026, 4, 2), 3.8}
    );
    given(mealRepository.findWeeklyTrend(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(weeklyTrend);

    // when
    StatisticsResponse response = statisticsService.getStatistics(MEMBER_ID);

    // then
    assertThat(response.totalMealCount()).isEqualTo(10);
    assertThat(response.reactionRate()).isEqualTo(50.0); // 5/10 * 100
    assertThat(response.topTags()).hasSize(3);
    assertThat(response.topTags().get(0).tag()).isEqualTo("비빔밥");
    assertThat(response.topTags().get(0).count()).isEqualTo(8);
    assertThat(response.weeklyTrend()).hasSize(2);
    assertThat(response.weeklyTrend().get(0).date()).isEqualTo(LocalDate.of(2026, 4, 1));
  }

  @Test
  void getStatistics_success_noReactions() {
    // given
    given(mealRepository.countByMemberIdAndDeletedAtIsNull(MEMBER_ID)).willReturn(5L);

    List<Meal> meals = List.of(createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now()));
    given(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(meals);

    given(reactionRepository.countByMealIdIn(List.of(1L))).willReturn(0L);
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(Collections.emptyList());
    given(mealRepository.findWeeklyTrend(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(Collections.emptyList());

    // when
    StatisticsResponse response = statisticsService.getStatistics(MEMBER_ID);

    // then
    assertThat(response.reactionRate()).isEqualTo(0.0);
    assertThat(response.topTags()).isEmpty();
    assertThat(response.weeklyTrend()).isEmpty();
  }

  @Test
  void getStatistics_success_topTagsLimitedTo5() {
    // given
    given(mealRepository.countByMemberIdAndDeletedAtIsNull(MEMBER_ID)).willReturn(20L);

    List<Meal> meals = List.of(createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now()));
    given(mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(meals);

    given(reactionRepository.countByMealIdIn(List.of(1L))).willReturn(1L);

    List<Object[]> tagStats = List.of(
        new Object[]{MEMBER_ID, "음식1", 4.5, 10},
        new Object[]{MEMBER_ID, "음식2", 4.3, 9},
        new Object[]{MEMBER_ID, "음식3", 4.1, 8},
        new Object[]{MEMBER_ID, "음식4", 3.9, 7},
        new Object[]{MEMBER_ID, "음식5", 3.7, 6},
        new Object[]{MEMBER_ID, "음식6", 3.5, 5},
        new Object[]{MEMBER_ID, "음식7", 3.3, 4}
    );
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(tagStats);
    given(mealRepository.findWeeklyTrend(
        org.mockito.ArgumentMatchers.eq(MEMBER_ID),
        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
        .willReturn(Collections.emptyList());

    // when
    StatisticsResponse response = statisticsService.getStatistics(MEMBER_ID);

    // then
    assertThat(response.topTags()).hasSize(5); // 최대 5개
  }

  // --- Helper ---

  private Meal createMeal(Long id, Long memberId, MealType mealType, LocalDateTime mealTime) {
    return Meal.builder()
        .id(id)
        .memberId(memberId)
        .mealType(mealType)
        .mealTime(mealTime)
        .photoUrl("https://s3.example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .analysisStatus(AnalysisStatus.COMPLETED)
        .build();
  }
}
