package com.mymealserver.common.test.fixtures;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test fixture for Ranking feature tests
 * Provides reusable Meal and Reaction instances with various scores for ranking tests
 */
public class RankingFixture {

    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2025, 2, 1, 12, 0);

    /**
     * Creates meals with various scores: [5.0, 4.5, 3.0, 2.0, 1.0]
     * Used for testing basic ranking functionality
     */
    public static List<Meal> createMealsWithVariousScores() {
        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 1))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(BASE_DATE.plusHours(i))
                    .photoUrl("https://example.com/meal" + (i + 1) + ".jpg")
                    .photoKey("meal" + (i + 1) + ".jpg")
                    .memo("Test meal " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions with scores corresponding to createMealsWithVariousScores()
     * Scores: [5.0, 4.5, 3.0, 2.0, 1.0]
     */
    public static List<Reaction> createReactionsWithScores() {
        List<Reaction> reactions = new ArrayList<>();
        double[] scores = {5.0, 4.5, 3.0, 2.0, 1.0};
        GradeType[] grades = {GradeType.GOOD, GradeType.GOOD, GradeType.NORMAL, GradeType.NORMAL, GradeType.BAD};

        for (int i = 0; i < 5; i++) {
            reactions.add(Reaction.builder()
                    .id((long) (i + 1))
                    .mealId((long) (i + 1))
                    .digestionLevel((short) (scores[i] > 3 ? 5 : 2))
                    .fullnessLevel((short) (scores[i] > 3 ? 5 : 2))
                    .energyLevel((short) (scores[i] > 3 ? 5 : 2))
                    .hasHeartburn(scores[i] < 2)
                    .hasGas(scores[i] < 2)
                    .hasBloating(scores[i] < 2.5)
                    .hasHeadache(scores[i] < 2)
                    .overallScore(scores[i])
                    .grade(grades[i])
                    .build());
        }
        return reactions;
    }

    /**
     * Creates a list of Meal and Reaction pairs for complete testing
     * Combines meals and reactions with matching IDs
     */
    public static List<MealReactionPair> createMealReactionPairs() {
        List<Meal> meals = createMealsWithVariousScores();
        List<Reaction> reactions = createReactionsWithScores();

        List<MealReactionPair> pairs = new ArrayList<>();
        for (int i = 0; i < meals.size(); i++) {
            pairs.add(new MealReactionPair(meals.get(i), reactions.get(i)));
        }
        return pairs;
    }

    /**
     * Creates meals by meal type for filtering tests
     * Returns 4 meals (BREAKFAST, LUNCH, DINNER, SNACK)
     */
    public static List<Meal> createMealsByType() {
        List<Meal> meals = new ArrayList<>();
        MealType[] types = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK};

        for (int i = 0; i < 4; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 10))
                    .memberId(1L)
                    .mealType(types[i])
                    .mealTime(BASE_DATE.plusDays(i))
                    .photoUrl("https://example.com/meal" + (i + 10) + ".jpg")
                    .photoKey("meal" + (i + 10) + ".jpg")
                    .memo(types[i].getDescription() + " meal")
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions for createMealsByType()
     * Scores: [4.0, 3.5, 3.0, 2.5]
     */
    public static List<Reaction> createReactionsByType() {
        List<Reaction> reactions = new ArrayList<>();
        double[] scores = {4.0, 3.5, 3.0, 2.5};
        GradeType[] grades = {GradeType.GOOD, GradeType.NORMAL, GradeType.NORMAL, GradeType.NORMAL};

        for (int i = 0; i < 4; i++) {
            reactions.add(Reaction.builder()
                    .id((long) (i + 10))
                    .mealId((long) (i + 10))
                    .digestionLevel((short) 4)
                    .fullnessLevel((short) 4)
                    .energyLevel((short) 4)
                    .hasHeartburn(false)
                    .hasGas(false)
                    .hasBloating(false)
                    .hasHeadache(false)
                    .overallScore(scores[i])
                    .grade(grades[i])
                    .build());
        }
        return reactions;
    }

    /**
     * Creates meals with boundary scores (maximum and minimum possible)
     * Scores: [5.0, 1.0, 3.0 (middle)]
     */
    public static List<Meal> createBoundaryScoreMeals() {
        List<Meal> meals = new ArrayList<>();
        double[] scores = {5.0, 1.0, 3.0};

        for (int i = 0; i < 3; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 20))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(BASE_DATE.plusDays(i))
                    .photoUrl("https://example.com/meal" + (i + 20) + ".jpg")
                    .photoKey("meal" + (i + 20) + ".jpg")
                    .memo("Boundary test meal " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions for createBoundaryScoreMeals()
     */
    public static List<Reaction> createBoundaryReactions() {
        List<Reaction> reactions = new ArrayList<>();
        double[] scores = {5.0, 1.0, 3.0};
        GradeType[] grades = {GradeType.GOOD, GradeType.BAD, GradeType.NORMAL};

        for (int i = 0; i < 3; i++) {
            reactions.add(Reaction.builder()
                    .id((long) (i + 20))
                    .mealId((long) (i + 20))
                    .digestionLevel((short) (scores[i] >= 3 ? 5 : 1))
                    .fullnessLevel((short) (scores[i] >= 3 ? 5 : 1))
                    .energyLevel((short) (scores[i] >= 3 ? 5 : 1))
                    .hasHeartburn(scores[i] < 2)
                    .hasGas(scores[i] < 2)
                    .hasBloating(scores[i] < 2.5)
                    .hasHeadache(scores[i] < 2)
                    .overallScore(scores[i])
                    .grade(grades[i])
                    .build());
        }
        return reactions;
    }

    /**
     * Creates a large set of meals (25) for pagination testing
     * Scores descending: 5.0, 4.9, 4.8, ..., 3.6, 3.5, 3.4
     */
    public static List<Meal> createLargeMealSet() {
        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 100))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(BASE_DATE.plusMinutes(i * 10))
                    .photoUrl("https://example.com/meal" + (i + 100) + ".jpg")
                    .photoKey("meal" + (i + 100) + ".jpg")
                    .memo("Pagination test meal " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions for createLargeMealSet()
     * Scores: 5.0 to 2.6 (descending by 0.1)
     */
    public static List<Reaction> createLargeReactionSet() {
        List<Reaction> reactions = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            double score = Math.round((5.0 - i * 0.1) * 100.0) / 100.0;
            GradeType grade = score >= 4 ? GradeType.GOOD : (score >= 2.5 ? GradeType.NORMAL : GradeType.BAD);

            reactions.add(Reaction.builder()
                    .id((long) (i + 100))
                    .mealId((long) (i + 100))
                    .digestionLevel((short) (score >= 3 ? 5 : 2))
                    .fullnessLevel((short) (score >= 3 ? 5 : 2))
                    .energyLevel((short) (score >= 3 ? 5 : 2))
                    .hasHeartburn(score < 2)
                    .hasGas(score < 2)
                    .hasBloating(score < 2.5)
                    .hasHeadache(score < 2)
                    .overallScore(score)
                    .grade(grade)
                    .build());
        }
        return reactions;
    }

    /**
     * Creates meals for date range filtering tests
     * Spans across 3 months: 2025-01-15, 2025-02-15, 2025-03-15
     */
    public static List<Meal> createMealsForDateRange() {
        List<Meal> meals = new ArrayList<>();
        LocalDateTime[] dates = {
                LocalDateTime.of(2025, 1, 15, 12, 0),
                LocalDateTime.of(2025, 2, 15, 12, 0),
                LocalDateTime.of(2025, 3, 15, 12, 0)
        };

        for (int i = 0; i < 3; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 30))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(dates[i])
                    .photoUrl("https://example.com/meal" + (i + 30) + ".jpg")
                    .photoKey("meal" + (i + 30) + ".jpg")
                    .memo("Date range test meal " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions for createMealsForDateRange()
     * Scores: [4.0, 3.0, 2.0]
     */
    public static List<Reaction> createReactionsForDateRange() {
        List<Reaction> reactions = new ArrayList<>();
        double[] scores = {4.0, 3.0, 2.0};
        GradeType[] grades = {GradeType.GOOD, GradeType.NORMAL, GradeType.NORMAL};

        for (int i = 0; i < 3; i++) {
            reactions.add(Reaction.builder()
                    .id((long) (i + 30))
                    .mealId((long) (i + 30))
                    .digestionLevel((short) (scores[i] >= 3 ? 4 : 2))
                    .fullnessLevel((short) (scores[i] >= 3 ? 4 : 2))
                    .energyLevel((short) (scores[i] >= 3 ? 4 : 2))
                    .hasHeartburn(scores[i] < 2)
                    .hasGas(scores[i] < 2)
                    .hasBloating(scores[i] < 2.5)
                    .hasHeadache(scores[i] < 2)
                    .overallScore(scores[i])
                    .grade(grades[i])
                    .build());
        }
        return reactions;
    }

    /**
     * Creates meals with same scores for testing tie-breaking by mealTime
     * Creates 3 meals with score 3.0 but different times
     */
    public static List<Meal> createMealsWithSameScores() {
        List<Meal> meals = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 40))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(BASE_DATE.plusHours(i * 2)) // Different times
                    .photoUrl("https://example.com/meal" + (i + 40) + ".jpg")
                    .photoKey("meal" + (i + 40) + ".jpg")
                    .memo("Same score meal " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Creates reactions with identical scores for createMealsWithSameScores()
     * All reactions have score 3.0 (NORMAL)
     */
    public static List<Reaction> createReactionsWithSameScores() {
        List<Reaction> reactions = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            reactions.add(Reaction.builder()
                    .id((long) (i + 40))
                    .mealId((long) (i + 40))
                    .digestionLevel((short) 3)
                    .fullnessLevel((short) 3)
                    .energyLevel((short) 3)
                    .hasHeartburn(false)
                    .hasGas(false)
                    .hasBloating(false)
                    .hasHeadache(false)
                    .overallScore(3.0)
                    .grade(GradeType.NORMAL)
                    .build());
        }
        return reactions;
    }

    /**
     * Creates meals without reactions for testing filtering
     * These meals should be excluded from ranking results
     */
    public static List<Meal> createMealsWithoutReactions() {
        List<Meal> meals = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            meals.add(Meal.builder()
                    .id((long) (i + 50))
                    .memberId(1L)
                    .mealType(MealType.LUNCH)
                    .mealTime(BASE_DATE.plusDays(i))
                    .photoUrl("https://example.com/meal" + (i + 50) + ".jpg")
                    .photoKey("meal" + (i + 50) + ".jpg")
                    .memo("Meal without reaction " + (i + 1))
                    .build());
        }
        return meals;
    }

    /**
     * Helper class to pair Meal with its Reaction
     */
    public static class MealReactionPair {
        private final Meal meal;
        private final Reaction reaction;

        public MealReactionPair(Meal meal, Reaction reaction) {
            this.meal = meal;
            this.reaction = reaction;
        }

        public Meal getMeal() {
            return meal;
        }

        public Reaction getReaction() {
            return reaction;
        }
    }

    /**
     * Sorts MealReactionPairs by score in descending order (best first)
     */
    public static List<MealReactionPair> sortByScoreDesc(List<MealReactionPair> pairs) {
        return pairs.stream()
                .sorted(Comparator.comparing(
                        (MealReactionPair p) -> p.getReaction().getOverallScore(),
                        Comparator.nullsLast(Comparator.reverseOrder())
                ).thenComparing(
                        (MealReactionPair p) -> p.getMeal().getMealTime(),
                        Comparator.reverseOrder()
                ))
                .toList();
    }

    /**
     * Sorts MealReactionPairs by score in ascending order (worst first)
     */
    public static List<MealReactionPair> sortByScoreAsc(List<MealReactionPair> pairs) {
        return pairs.stream()
                .sorted(Comparator.comparing(
                        (MealReactionPair p) -> p.getReaction().getOverallScore(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).thenComparing(
                        (MealReactionPair p) -> p.getMeal().getMealTime(),
                        Comparator.reverseOrder()
                ))
                .toList();
    }

    /**
     * Extracts meal IDs from a list of MealReactionPairs
     */
    public static List<Long> extractMealIds(List<MealReactionPair> pairs) {
        return pairs.stream()
                .map(p -> p.getMeal().getId())
                .toList();
    }

    /**
     * Extracts scores from a list of MealReactionPairs
     */
    public static List<Double> extractScores(List<MealReactionPair> pairs) {
        return pairs.stream()
                .map(p -> p.getReaction().getOverallScore())
                .toList();
    }
}
