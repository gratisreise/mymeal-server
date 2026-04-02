package api.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.mymealserver.api.calendar.dto.CalendarDailyResponse;
import com.mymealserver.api.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.api.calendar.service.CalendarDataAggregator;
import com.mymealserver.api.calendar.service.CalendarReader;
import com.mymealserver.api.calendar.service.CalendarService;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.reaction.Reaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

  @Mock private CalendarReader calendarReader;
  @Mock private CalendarDataAggregator dataAggregator;

  @InjectMocks private CalendarService calendarService;

  private static final Long MEMBER_ID = 1L;

  // ========================
  // getMonthlyCalendar
  // ========================

  @Test
  void getMonthlyCalendar_success() {
    // given
    int year = 2026;
    int month = 4;
    LocalDate day1 = LocalDate.of(2026, 4, 1);
    LocalDate day2 = LocalDate.of(2026, 4, 15);

    Meal meal1 = createMeal(1L, MealType.BREAKFAST, day1.atTime(8, 0));
    Meal meal2 = createMeal(2L, MealType.LUNCH, day1.atTime(12, 0));
    Meal meal3 = createMeal(3L, MealType.DINNER, day2.atTime(18, 0));

    Reaction reaction1 = createReaction(10L, 1L, 4.5);
    Reaction reaction2 = createReaction(20L, 2L, 3.0);
    Reaction reaction3 = createReaction(30L, 3L, 2.0);

    Map<Long, Reaction> reactionsByMealId =
        Map.of(
            1L, reaction1,
            2L, reaction2,
            3L, reaction3);

    Map<LocalDate, List<Meal>> mealsByDate =
        Map.of(
            day1, List.of(meal1, meal2),
            day2, List.of(meal3));

    List<Reaction> day1Reactions = List.of(reaction1, reaction2);

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID,
                LocalDate.of(2026, 4, 1).atStartOfDay(),
                LocalDate.of(2026, 4, 30).atTime(23, 59, 59)))
        .willReturn(List.of(meal1, meal2, meal3));

    given(calendarReader.findReactionsByMealIds(List.of(1L, 2L, 3L))).willReturn(reactionsByMealId);

    given(calendarReader.groupMealsByDate(List.of(meal1, meal2, meal3))).willReturn(mealsByDate);

    // Day 1 aggregation stubs
    given(dataAggregator.filterValidReactions(List.of(meal1, meal2), reactionsByMealId))
        .willReturn(day1Reactions);
    given(dataAggregator.calculateAverageScore(day1Reactions)).willReturn(3.75);
    given(dataAggregator.classifyQuality(3.75)).willReturn(GradeType.NORMAL);
    given(dataAggregator.extractMealTypes(List.of(meal1, meal2)))
        .willReturn(java.util.Set.of(MealType.BREAKFAST, MealType.LUNCH));
    given(dataAggregator.calculateReactionRate(2, 2)).willReturn(100.0);

    // Day 2 aggregation stubs
    List<Reaction> day2Reactions = List.of(reaction3);
    given(dataAggregator.filterValidReactions(List.of(meal3), reactionsByMealId))
        .willReturn(day2Reactions);
    given(dataAggregator.calculateAverageScore(day2Reactions)).willReturn(2.0);
    given(dataAggregator.classifyQuality(2.0)).willReturn(GradeType.BAD);
    given(dataAggregator.extractMealTypes(List.of(meal3)))
        .willReturn(java.util.Set.of(MealType.DINNER));
    given(dataAggregator.calculateReactionRate(1, 1)).willReturn(100.0);

    // when
    CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(MEMBER_ID, year, month);

    // then
    assertThat(response.year()).isEqualTo(2026);
    assertThat(response.month()).isEqualTo(4);
    assertThat(response.days()).hasSize(2);
    assertThat(response.days().get(day1).mealCount()).isEqualTo(2);
    assertThat(response.days().get(day1).quality()).isEqualTo(GradeType.NORMAL);
    assertThat(response.days().get(day1).reactionRate()).isEqualTo(100.0);
    assertThat(response.days().get(day2).mealCount()).isEqualTo(1);
    assertThat(response.days().get(day2).quality()).isEqualTo(GradeType.BAD);
  }

  @Test
  void getMonthlyCalendar_success_emptyMonth() {
    // given
    int year = 2026;
    int month = 3;

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID,
                LocalDate.of(2026, 3, 1).atStartOfDay(),
                LocalDate.of(2026, 3, 31).atTime(23, 59, 59)))
        .willReturn(List.of());

    given(calendarReader.findReactionsByMealIds(List.of())).willReturn(Map.of());
    given(calendarReader.groupMealsByDate(List.of())).willReturn(Map.of());

    // when
    CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(MEMBER_ID, year, month);

    // then
    assertThat(response.year()).isEqualTo(2026);
    assertThat(response.month()).isEqualTo(3);
    assertThat(response.days()).isEmpty();
  }

  @Test
  void getMonthlyCalendar_success_partialDays() {
    // given
    int year = 2026;
    int month = 4;
    LocalDate day5 = LocalDate.of(2026, 4, 5);
    LocalDate day20 = LocalDate.of(2026, 4, 20);

    Meal meal1 = createMeal(1L, MealType.BREAKFAST, day5.atTime(8, 0));
    Meal meal2 = createMeal(2L, MealType.LUNCH, day20.atTime(12, 0));

    Map<Long, Reaction> reactionsByMealId = Map.of();
    Map<LocalDate, List<Meal>> mealsByDate =
        Map.of(
            day5, List.of(meal1),
            day20, List.of(meal2));

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID,
                LocalDate.of(2026, 4, 1).atStartOfDay(),
                LocalDate.of(2026, 4, 30).atTime(23, 59, 59)))
        .willReturn(List.of(meal1, meal2));

    given(calendarReader.findReactionsByMealIds(List.of(1L, 2L))).willReturn(reactionsByMealId);
    given(calendarReader.groupMealsByDate(List.of(meal1, meal2))).willReturn(mealsByDate);

    // Day 5 stubs (no reactions)
    given(dataAggregator.filterValidReactions(List.of(meal1), reactionsByMealId))
        .willReturn(List.of());
    given(dataAggregator.calculateAverageScore(List.of())).willReturn(null);
    given(dataAggregator.classifyQuality(null)).willReturn(null);
    given(dataAggregator.extractMealTypes(List.of(meal1)))
        .willReturn(java.util.Set.of(MealType.BREAKFAST));
    given(dataAggregator.calculateReactionRate(1, 0)).willReturn(0.0);

    // Day 20 stubs (no reactions)
    given(dataAggregator.filterValidReactions(List.of(meal2), reactionsByMealId))
        .willReturn(List.of());
    given(dataAggregator.calculateAverageScore(List.of())).willReturn(null);
    given(dataAggregator.classifyQuality(null)).willReturn(null);
    given(dataAggregator.extractMealTypes(List.of(meal2)))
        .willReturn(java.util.Set.of(MealType.LUNCH));
    given(dataAggregator.calculateReactionRate(1, 0)).willReturn(0.0);

    // when
    CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(MEMBER_ID, year, month);

    // then
    assertThat(response.days()).hasSize(2);
    assertThat(response.days().keySet()).containsExactlyInAnyOrder(day5, day20);
    assertThat(response.days().get(day5).averageScore()).isNull();
    assertThat(response.days().get(day5).quality()).isNull();
    assertThat(response.days().get(day5).reactionRate()).isEqualTo(0.0);
  }

  // ========================
  // getDailyCalendar
  // ========================

  @Test
  void getDailyCalendar_success() {
    // given
    LocalDate date = LocalDate.of(2026, 4, 1);

    Meal meal1 = createMeal(1L, MealType.BREAKFAST, date.atTime(8, 0));
    Meal meal2 = createMeal(2L, MealType.LUNCH, date.atTime(12, 0));

    Reaction reaction1 = createReaction(10L, 1L, 4.5);
    MealAnalysis analysis1 = createMealAnalysis(100L, 1L, "된장찌개");

    Map<Long, Reaction> reactionsByMealId = Map.of(1L, reaction1);
    Map<Long, MealAnalysis> analysesByMealId = Map.of(1L, analysis1);

    List<Reaction> validReactions = List.of(reaction1);

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID, date.atStartOfDay(), date.atTime(23, 59, 59)))
        .willReturn(List.of(meal1, meal2));

    given(calendarReader.findReactionsByMealIds(List.of(1L, 2L))).willReturn(reactionsByMealId);
    given(calendarReader.findMealAnalysesByMealIds(List.of(1L, 2L))).willReturn(analysesByMealId);

    // Statistics stubs
    given(dataAggregator.filterValidReactions(List.of(meal1, meal2), reactionsByMealId))
        .willReturn(validReactions);
    given(dataAggregator.calculateAverageScore(validReactions)).willReturn(4.5);
    given(dataAggregator.calculateReactionRate(2, 1)).willReturn(50.0);

    // when
    CalendarDailyResponse response = calendarService.getDailyCalendar(MEMBER_ID, date);

    // then
    assertThat(response.date()).isEqualTo(date);
    assertThat(response.meals()).hasSize(2);
    assertThat(response.statistics().mealCount()).isEqualTo(2);
    assertThat(response.statistics().averageScore()).isEqualTo(4.5);
    assertThat(response.statistics().reactionRate()).isEqualTo(50.0);

    // Meal 1: has reaction + analysis
    assertThat(response.meals().get(0).hasReaction()).isTrue();
    assertThat(response.meals().get(0).aiAnalysis()).isNotNull();
    assertThat(response.meals().get(0).reaction()).isNotNull();

    // Meal 2: no reaction, no analysis
    assertThat(response.meals().get(1).hasReaction()).isFalse();
    assertThat(response.meals().get(1).aiAnalysis()).isNull();
    assertThat(response.meals().get(1).reaction()).isNull();
  }

  @Test
  void getDailyCalendar_success_noMeals() {
    // given
    LocalDate date = LocalDate.of(2026, 4, 1);

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID, date.atStartOfDay(), date.atTime(23, 59, 59)))
        .willReturn(List.of());

    given(calendarReader.findReactionsByMealIds(List.of())).willReturn(Map.of());
    given(calendarReader.findMealAnalysesByMealIds(List.of())).willReturn(Map.of());

    given(dataAggregator.filterValidReactions(List.of(), Map.of())).willReturn(List.of());
    given(dataAggregator.calculateAverageScore(List.of())).willReturn(null);
    given(dataAggregator.calculateReactionRate(0, 0)).willReturn(0.0);

    // when
    CalendarDailyResponse response = calendarService.getDailyCalendar(MEMBER_ID, date);

    // then
    assertThat(response.date()).isEqualTo(date);
    assertThat(response.meals()).isEmpty();
    assertThat(response.statistics().mealCount()).isEqualTo(0);
    assertThat(response.statistics().averageScore()).isNull();
    assertThat(response.statistics().reactionRate()).isEqualTo(0.0);
  }

  @Test
  void getDailyCalendar_success_withAnalysisAndReaction() {
    // given
    LocalDate date = LocalDate.of(2026, 4, 1);
    Meal meal = createMeal(1L, MealType.LUNCH, date.atTime(12, 0));
    Reaction reaction = createReaction(10L, 1L, 3.5);
    MealAnalysis analysis = createMealAnalysis(100L, 1L, "불고기");

    Map<Long, Reaction> reactionsByMealId = Map.of(1L, reaction);
    Map<Long, MealAnalysis> analysesByMealId = Map.of(1L, analysis);

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID, date.atStartOfDay(), date.atTime(23, 59, 59)))
        .willReturn(List.of(meal));
    given(calendarReader.findReactionsByMealIds(List.of(1L))).willReturn(reactionsByMealId);
    given(calendarReader.findMealAnalysesByMealIds(List.of(1L))).willReturn(analysesByMealId);

    given(dataAggregator.filterValidReactions(List.of(meal), reactionsByMealId))
        .willReturn(List.of(reaction));
    given(dataAggregator.calculateAverageScore(List.of(reaction))).willReturn(3.5);
    given(dataAggregator.calculateReactionRate(1, 1)).willReturn(100.0);

    // when
    CalendarDailyResponse response = calendarService.getDailyCalendar(MEMBER_ID, date);

    // then
    assertThat(response.meals()).hasSize(1);
    assertThat(response.meals().get(0).hasReaction()).isTrue();
    assertThat(response.meals().get(0).aiAnalysis()).isNotNull();
    assertThat(response.meals().get(0).aiAnalysis().mealName()).isEqualTo("불고기");
    assertThat(response.meals().get(0).reaction()).isNotNull();
    assertThat(response.meals().get(0).reaction().overallScore()).isEqualTo(3.5);
  }

  @Test
  void getDailyCalendar_success_withoutAnalysisAndReaction() {
    // given
    LocalDate date = LocalDate.of(2026, 4, 1);
    Meal meal = createMeal(1L, MealType.DINNER, date.atTime(18, 0));

    given(
            calendarReader.findMealsByDateRange(
                MEMBER_ID, date.atStartOfDay(), date.atTime(23, 59, 59)))
        .willReturn(List.of(meal));
    given(calendarReader.findReactionsByMealIds(List.of(1L))).willReturn(Map.of());
    given(calendarReader.findMealAnalysesByMealIds(List.of(1L))).willReturn(Map.of());

    given(dataAggregator.filterValidReactions(List.of(meal), Map.of())).willReturn(List.of());
    given(dataAggregator.calculateAverageScore(List.of())).willReturn(null);
    given(dataAggregator.calculateReactionRate(1, 0)).willReturn(0.0);

    // when
    CalendarDailyResponse response = calendarService.getDailyCalendar(MEMBER_ID, date);

    // then
    assertThat(response.meals()).hasSize(1);
    assertThat(response.meals().get(0).hasReaction()).isFalse();
    assertThat(response.meals().get(0).aiAnalysis()).isNull();
    assertThat(response.meals().get(0).reaction()).isNull();
    assertThat(response.statistics().averageScore()).isNull();
    assertThat(response.statistics().reactionRate()).isEqualTo(0.0);
  }

  // --- Helper methods ---

  private Meal createMeal(Long id, MealType mealType, LocalDateTime mealTime) {
    return Meal.builder()
        .id(id)
        .memberId(MEMBER_ID)
        .mealType(mealType)
        .mealTime(mealTime)
        .photoUrl("https://example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .build();
  }

  private Reaction createReaction(Long id, Long mealId, Double overallScore) {
    Reaction reaction =
        Reaction.builder()
            .id(id)
            .mealId(mealId)
            .digestionLevel((short) 4)
            .fullnessLevel((short) 3)
            .energyLevel((short) 4)
            .build();
    reaction.setOverallScore(overallScore);
    return reaction;
  }

  private MealAnalysis createMealAnalysis(Long id, Long mealId, String mealName) {
    return MealAnalysis.builder()
        .id(id)
        .mealId(mealId)
        .mealName(mealName)
        .calories(500.0)
        .carbohydrates(60.0)
        .protein(30.0)
        .fat(15.0)
        .build();
  }
}
