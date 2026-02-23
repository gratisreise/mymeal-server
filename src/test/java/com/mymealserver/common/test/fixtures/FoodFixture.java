package com.mymealserver.common.test.fixtures;

import com.mymealserver.domain.food.Food;

/**
 * Test fixture for Food entities
 * Provides reusable Food instances for testing
 */
public class FoodFixture {

    /**
     * Creates a default Korean stew (Kimchi Stew)
     */
    public static Food createKimchiStew() {
        return Food.builder()
                .id(100L)
                .name("김치찌개")
                .calories(450.0)
                .carbohydrates(65.0)
                .protein(25.0)
                .fat(12.0)
                .averageScore(3.5)
                .mealCount(10)
                .build();
    }

    /**
     * Creates Bibimbap
     */
    public static Food createBibimbap() {
        return Food.builder()
                .id(101L)
                .name("비빔밥")
                .calories(550.0)
                .carbohydrates(70.0)
                .protein(18.0)
                .fat(15.0)
                .averageScore(4.0)
                .mealCount(5)
                .build();
    }

    /**
     * Creates Doenjang Stew
     */
    public static Food createDoenjangStew() {
        return Food.builder()
                .id(102L)
                .name("된장찌개")
                .calories(380.0)
                .carbohydrates(45.0)
                .protein(22.0)
                .fat(14.0)
                .averageScore(3.8)
                .mealCount(8)
                .build();
    }

    /**
     * Creates a custom food with specified values
     */
    public static Food createCustomFood(
            Long id,
            String name,
            double calories,
            double carbohydrates,
            double protein,
            double fat
    ) {
        return Food.builder()
                .id(id)
                .name(name)
                .calories(calories)
                .carbohydrates(carbohydrates)
                .protein(protein)
                .fat(fat)
                .averageScore(0.0)
                .mealCount(0)
                .build();
    }

    /**
     * Creates a new food (not yet saved, no ID)
     * Used for testing auto-creation of new foods
     */
    public static Food createNewFoodForAutoCreation(String name, double calories) {
        return Food.builder()
                .id(null) // Not saved yet
                .name(name)
                .calories(calories)
                .carbohydrates(50.0)
                .protein(20.0)
                .fat(10.0)
                .averageScore(0.0)
                .mealCount(0)
                .build();
    }
}
