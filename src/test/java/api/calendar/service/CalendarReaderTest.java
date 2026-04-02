package api.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.mymealserver.api.calendar.service.CalendarReader;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
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
class CalendarReaderTest {

  @Mock private MealReader mealReader;
  @Mock private ReactionReader reactionReader;
  @Mock private MealAnalysisReader mealAnalysisReader;

  @InjectMocks private CalendarReader calendarReader;

  // --- findMealsByDateRange ---

  @Test
  void findMealsByDateRange_success() {
    // given
    Long memberId = 1L;
    LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59, 59);
    List<Meal> meals = List.of(
        createMeal(1L, MealType.BREAKFAST, LocalDateTime.of(2026, 4, 1, 8, 0)),
        createMeal(2L, MealType.LUNCH, LocalDateTime.of(2026, 4, 1, 12, 0)));
    given(mealReader.findByMemberIdAndDateRange(memberId, start, end)).willReturn(meals);

    // when
    List<Meal> result = calendarReader.findMealsByDateRange(memberId, start, end);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(1).getId()).isEqualTo(2L);
  }

  @Test
  void findMealsByDateRange_empty() {
    // given
    Long memberId = 1L;
    LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59, 59);
    given(mealReader.findByMemberIdAndDateRange(memberId, start, end)).willReturn(List.of());

    // when
    List<Meal> result = calendarReader.findMealsByDateRange(memberId, start, end);

    // then
    assertThat(result).isEmpty();
  }

  // --- findReactionsByMealIds ---

  @Test
  void findReactionsByMealIds_success() {
    // given
    Reaction reaction1 = createReaction(10L, 1L);
    Reaction reaction2 = createReaction(20L, 2L);
    given(reactionReader.findAllByMealIds(List.of(1L, 2L)))
        .willReturn(List.of(reaction1, reaction2));

    // when
    Map<Long, Reaction> result = calendarReader.findReactionsByMealIds(List.of(1L, 2L));

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(1L)).isEqualTo(reaction1);
    assertThat(result.get(2L)).isEqualTo(reaction2);
  }

  @Test
  void findReactionsByMealIds_emptyMealIds() {
    // when
    Map<Long, Reaction> result = calendarReader.findReactionsByMealIds(List.of());

    // then
    assertThat(result).isEmpty();
  }

  // --- findMealAnalysesByMealIds ---

  @Test
  void findMealAnalysesByMealIds_success() {
    // given
    MealAnalysis analysis1 = createMealAnalysis(100L, 1L, "된장찌개");
    MealAnalysis analysis2 = createMealAnalysis(200L, 2L, "김치찌개");
    given(mealAnalysisReader.findAllByMealIds(List.of(1L, 2L)))
        .willReturn(List.of(analysis1, analysis2));

    // when
    Map<Long, MealAnalysis> result = calendarReader.findMealAnalysesByMealIds(List.of(1L, 2L));

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(1L).getMealName()).isEqualTo("된장찌개");
    assertThat(result.get(2L).getMealName()).isEqualTo("김치찌개");
  }

  @Test
  void findMealAnalysesByMealIds_emptyMealIds() {
    // when
    Map<Long, MealAnalysis> result = calendarReader.findMealAnalysesByMealIds(List.of());

    // then
    assertThat(result).isEmpty();
  }

  // --- groupMealsByDate ---

  @Test
  void groupMealsByDate_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST, LocalDateTime.of(2026, 4, 1, 8, 0));
    Meal meal2 = createMeal(2L, MealType.LUNCH, LocalDateTime.of(2026, 4, 1, 12, 0));
    Meal meal3 = createMeal(3L, MealType.DINNER, LocalDateTime.of(2026, 4, 2, 18, 0));

    // when
    Map<LocalDate, List<Meal>> result = calendarReader.groupMealsByDate(List.of(meal1, meal2, meal3));

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(LocalDate.of(2026, 4, 1))).hasSize(2);
    assertThat(result.get(LocalDate.of(2026, 4, 2))).hasSize(1);
  }

  @Test
  void groupMealsByDate_emptyMeals() {
    // when
    Map<LocalDate, List<Meal>> result = calendarReader.groupMealsByDate(List.of());

    // then
    assertThat(result).isEmpty();
  }

  // --- Helper methods ---

  private Meal createMeal(Long id, MealType mealType, LocalDateTime mealTime) {
    return Meal.builder()
        .id(id)
        .memberId(1L)
        .mealType(mealType)
        .mealTime(mealTime)
        .photoUrl("https://example.com/photo.jpg")
        .photoKey("photo.jpg")
        .build();
  }

  private Reaction createReaction(Long id, Long mealId) {
    return Reaction.builder()
        .id(id)
        .mealId(mealId)
        .digestionLevel((short) 4)
        .fullnessLevel((short) 3)
        .energyLevel((short) 4)
        .build();
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
