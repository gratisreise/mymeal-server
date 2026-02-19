package com.mymealserver.common.test.fixtures;

import com.mymealserver.entity.Meal;
import com.mymealserver.entity.enums.AnalysisStatus;
import com.mymealserver.entity.enums.MealType;

import java.time.LocalDateTime;

/**
 * Test fixture for Meal entities
 * Provides reusable Meal instances for testing
 */
public class MealFixture {

    /**
     * Creates a default breakfast meal
     */
    public static Meal createDefaultBreakfast() {
        return Meal.builder()
                .id(1L)
                .memberId(1L)
                .mealType(MealType.BREAKFAST)
                .mealTime(LocalDateTime.of(2025, 2, 15, 8, 0))
                .photoUrl("https://example.com/breakfast.jpg")
                .photoKey("meal/breakfast.jpg")
                .analysisStatus(AnalysisStatus.PENDING)
                .build();
    }

    /**
     * Creates a default lunch meal
     */
    public static Meal createDefaultLunch() {
        return Meal.builder()
                .id(2L)
                .memberId(1L)
                .mealType(MealType.LUNCH)
                .mealTime(LocalDateTime.of(2025, 2, 15, 12, 30))
                .photoUrl("https://example.com/lunch.jpg")
                .photoKey("meal/lunch.jpg")
                .analysisStatus(AnalysisStatus.PENDING)
                .build();
    }

    /**
     * Creates a default dinner meal
     */
    public static Meal createDefaultDinner() {
        return Meal.builder()
                .id(3L)
                .memberId(1L)
                .mealType(MealType.DINNER)
                .mealTime(LocalDateTime.of(2025, 2, 15, 19, 0))
                .photoUrl("https://example.com/dinner.jpg")
                .photoKey("meal/dinner.jpg")
                .analysisStatus(AnalysisStatus.PENDING)
                .build();
    }

    /**
     * Creates a snack meal
     */
    public static Meal createDefaultSnack() {
        return Meal.builder()
                .id(4L)
                .memberId(1L)
                .mealType(MealType.SNACK)
                .mealTime(LocalDateTime.of(2025, 2, 15, 15, 0))
                .photoUrl("https://example.com/snack.jpg")
                .photoKey("meal/snack.jpg")
                .analysisStatus(AnalysisStatus.PENDING)
                .build();
    }

    /**
     * Creates a meal with custom fields
     */
    public static Meal createCustomMeal(
            Long id,
            Long memberId,
            MealType mealType,
            LocalDateTime mealTime
    ) {
        return Meal.builder()
                .id(id)
                .memberId(memberId)
                .mealType(mealType)
                .mealTime(mealTime)
                .photoUrl("https://example.com/meal.jpg")
                .photoKey("meal/meal.jpg")
                .analysisStatus(AnalysisStatus.PENDING)
                .build();
    }

    /**
     * Creates a meal with completed analysis
     */
    public static Meal createMealWithCompletedAnalysis() {
        return Meal.builder()
                .id(5L)
                .memberId(1L)
                .mealType(MealType.LUNCH)
                .mealTime(LocalDateTime.now())
                .photoUrl("https://example.com/analyzed.jpg")
                .photoKey("meal/analyzed.jpg")
                .analysisStatus(AnalysisStatus.COMPLETED)
                .build();
    }
}
