package com.mymealserver.dto.reaction;

import com.mymealserver.entity.enums.GradeType;

import java.time.LocalDateTime;

public record ReactionResponse(
        Long id,
        Long mealId,
        Integer digestionLevel,
        Integer fullnessLevel,
        Integer energyLevel,
        Boolean hasHeartburn,
        Boolean hasGas,
        Boolean hasBloating,
        Boolean hasHeadache,
        String memo,
        Double overallScore,
        GradeType grade,
        LocalDateTime createdAt
) {
}
