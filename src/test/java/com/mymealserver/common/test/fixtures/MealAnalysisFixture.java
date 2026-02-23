package com.mymealserver.common.test.fixtures;

import com.mymealserver.domain.mealanalysis.MealAnalysis;

/**
 * Test fixture for MealAnalysis entities
 * Provides reusable MealAnalysis instances for testing
 */
public class MealAnalysisFixture {

    /**
     * Creates a default meal analysis with typical values
     */
    public static MealAnalysis createDefaultAnalysis() {
        return MealAnalysis.builder()
                .id(1L)
                .mealId(1L)
                .foodId(100L)
                .mealName("김치찌개")
                .calories(450.0)
                .carbohydrates(65.0)
                .protein(25.0)
                .fat(12.0)
                .confidence(0.92)
                .rawResponse("{\"detected_food\": \"kimchi_stew\"}")
                .build();
    }

    /**
     * Creates a custom meal analysis with specified values
     */
    public static MealAnalysis createCustomAnalysis(
            Long id,
            Long mealId,
            String mealName,
            double calories
    ) {
        return MealAnalysis.builder()
                .id(id)
                .mealId(mealId)
                .foodId(null)
                .mealName(mealName)
                .calories(calories)
                .carbohydrates(50.0)
                .protein(20.0)
                .fat(10.0)
                .confidence(0.85)
                .rawResponse("{}")
                .build();
    }

    /**
     * Creates a meal analysis without foodId (food matching not yet done)
     */
    public static MealAnalysis createAnalysisWithoutFoodId() {
        return MealAnalysis.builder()
                .id(2L)
                .mealId(2L)
                .foodId(null)
                .mealName("비빔밥")
                .calories(550.0)
                .carbohydrates(70.0)
                .protein(18.0)
                .fat(15.0)
                .confidence(0.88)
                .rawResponse("{\"detected_food\": \"bibimbap\"}")
                .build();
    }

    /**
     * Creates a high confidence analysis
     */
    public static MealAnalysis createHighConfidenceAnalysis(Long mealId) {
        return MealAnalysis.builder()
                .id(3L)
                .mealId(mealId)
                .foodId(200L)
                .mealName("된장찌개")
                .calories(380.0)
                .carbohydrates(45.0)
                .protein(22.0)
                .fat(14.0)
                .confidence(0.95)
                .rawResponse("{\"detected_food\": \"doenjang_stew\"}")
                .build();
    }

    /**
     * Creates a low confidence analysis
     */
    public static MealAnalysis createLowConfidenceAnalysis(Long mealId) {
        return MealAnalysis.builder()
                .id(4L)
                .mealId(mealId)
                .foodId(null)
                .mealName("알 수 없는 음식")
                .calories(0.0)
                .carbohydrates(0.0)
                .protein(0.0)
                .fat(0.0)
                .confidence(0.45)
                .rawResponse("{\"detected_food\": \"unknown\"}")
                .build();
    }
}
