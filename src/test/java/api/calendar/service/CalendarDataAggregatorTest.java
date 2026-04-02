package api.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mymealserver.api.calendar.service.CalendarDataAggregator;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.reaction.Reaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarDataAggregatorTest {

  private final CalendarDataAggregator aggregator = new CalendarDataAggregator();

  // --- filterValidReactions ---

  @Test
  void filterValidReactions_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST);
    Meal meal2 = createMeal(2L, MealType.LUNCH);
    Meal meal3 = createMeal(3L, MealType.DINNER);

    Reaction reaction1 = createReaction(1L, 1L);
    Reaction reaction3 = createReaction(3L, 3L);

    Map<Long, Reaction> reactionsByMealId = Map.of(1L, reaction1, 3L, reaction3);

    // when
    List<Reaction> result = aggregator.filterValidReactions(List.of(meal1, meal2, meal3), reactionsByMealId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.stream().map(Reaction::getMealId)).containsExactlyInAnyOrder(1L, 3L);
  }

  @Test
  void filterValidReactions_allNull() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST);
    Meal meal2 = createMeal(2L, MealType.LUNCH);
    Map<Long, Reaction> reactionsByMealId = Map.of();

    // when
    List<Reaction> result = aggregator.filterValidReactions(List.of(meal1, meal2), reactionsByMealId);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void filterValidReactions_noNull() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST);
    Meal meal2 = createMeal(2L, MealType.LUNCH);
    Reaction reaction1 = createReaction(1L, 1L);
    Reaction reaction2 = createReaction(2L, 2L);
    Map<Long, Reaction> reactionsByMealId = Map.of(1L, reaction1, 2L, reaction2);

    // when
    List<Reaction> result = aggregator.filterValidReactions(List.of(meal1, meal2), reactionsByMealId);

    // then
    assertThat(result).hasSize(2);
  }

  // --- calculateAverageScore ---

  @Test
  void calculateAverageScore_success() {
    // given
    Reaction r1 = createReactionWithScore(1L, 4.0);
    Reaction r2 = createReactionWithScore(2L, 3.0);
    Reaction r3 = createReactionWithScore(3L, 5.0);

    // when
    Double result = aggregator.calculateAverageScore(List.of(r1, r2, r3));

    // then
    assertThat(result).isCloseTo(4.0, org.assertj.core.data.Offset.offset(0.01));
  }

  @Test
  void calculateAverageScore_emptyReactions() {
    // when
    Double result = aggregator.calculateAverageScore(List.of());

    // then
    assertThat(result).isNull();
  }

  // --- calculateReactionRate ---

  @Test
  void calculateReactionRate_success() {
    // when
    Double result = aggregator.calculateReactionRate(5, 3);

    // then
    assertThat(result).isEqualTo(60.0);
  }

  @Test
  void calculateReactionRate_zeroMeals() {
    // when
    Double result = aggregator.calculateReactionRate(0, 0);

    // then
    assertThat(result).isEqualTo(0.0);
  }

  @Test
  void calculateReactionRate_fullRate() {
    // when
    Double result = aggregator.calculateReactionRate(4, 4);

    // then
    assertThat(result).isEqualTo(100.0);
  }

  // --- classifyQuality ---

  @Test
  void classifyQuality_good() {
    assertThat(aggregator.classifyQuality(4.0)).isEqualTo(GradeType.GOOD);
    assertThat(aggregator.classifyQuality(4.5)).isEqualTo(GradeType.GOOD);
    assertThat(aggregator.classifyQuality(5.0)).isEqualTo(GradeType.GOOD);
  }

  @Test
  void classifyQuality_normal() {
    assertThat(aggregator.classifyQuality(2.5)).isEqualTo(GradeType.NORMAL);
    assertThat(aggregator.classifyQuality(3.0)).isEqualTo(GradeType.NORMAL);
    assertThat(aggregator.classifyQuality(3.9)).isEqualTo(GradeType.NORMAL);
  }

  @Test
  void classifyQuality_bad() {
    assertThat(aggregator.classifyQuality(2.4)).isEqualTo(GradeType.BAD);
    assertThat(aggregator.classifyQuality(1.0)).isEqualTo(GradeType.BAD);
  }

  @Test
  void classifyQuality_null() {
    assertThat(aggregator.classifyQuality(null)).isNull();
  }

  // --- extractMealTypes ---

  @Test
  void extractMealTypes_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST);
    Meal meal2 = createMeal(2L, MealType.LUNCH);
    Meal meal3 = createMeal(3L, MealType.BREAKFAST);

    // when
    Set<MealType> result = aggregator.extractMealTypes(List.of(meal1, meal2, meal3));

    // then
    assertThat(result).containsExactlyInAnyOrder(MealType.BREAKFAST, MealType.LUNCH);
  }

  // --- aggregateReactions ---

  @Test
  void aggregateReactions_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.BREAKFAST);
    Meal meal2 = createMeal(2L, MealType.LUNCH);
    Reaction reaction1 = createReaction(10L, 1L);
    Reaction reaction2 = createReaction(20L, 2L);
    Map<Long, Reaction> reactionsByMealId = Map.of(1L, reaction1, 2L, reaction2);

    // when
    Map<Long, Reaction> result = aggregator.aggregateReactions(List.of(meal1, meal2), reactionsByMealId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(1L)).isEqualTo(reaction1);
    assertThat(result.get(2L)).isEqualTo(reaction2);
  }

  // --- Helper methods ---

  private Meal createMeal(Long id, MealType mealType) {
    return Meal.builder()
        .id(id)
        .memberId(1L)
        .mealType(mealType)
        .mealTime(LocalDateTime.of(2026, 4, 1, 12, 0))
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

  private Reaction createReactionWithScore(Long mealId, Double overallScore) {
    Reaction reaction = Reaction.builder()
        .id(mealId * 100)
        .mealId(mealId)
        .digestionLevel((short) 4)
        .fullnessLevel((short) 3)
        .energyLevel((short) 4)
        .build();
    reaction.setOverallScore(overallScore);
    return reaction;
  }
}
