package com.mymealserver.dto.meal;

import java.util.List;

public record AIAnalysisResponse(
        String mealName,
        Double calories,
        NutrientsResponse nutrients,
        List<String> tags,
        Double confidence
) {
    public record NutrientsResponse(
            Double carbohydrates,
            Double protein,
            Double fat,
            Double fiber
    ) {
    }
}
