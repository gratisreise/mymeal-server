package com.mymealserver.api.reaction.dto.request;

import com.mymealserver.domain.reaction.Reaction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record ReactionRequest(

        @NotNull(message = "{validation.reaction.digestionLevel.notnull}")
        @Min(value = 1, message = "{validation.reaction.digestionLevel.range}")
        @Max(value = 5, message = "{validation.reaction.digestionLevel.range}")
        Integer digestionLevel,

        @NotNull(message = "{validation.reaction.fullnessLevel.notnull}")
        @Min(value = 1, message = "{validation.reaction.fullnessLevel.range}")
        @Max(value = 5, message = "{validation.reaction.fullnessLevel.range}")
        Integer fullnessLevel,

        @NotNull(message = "{validation.reaction.energyLevel.notnull}")
        @Min(value = 1, message = "{validation.reaction.energyLevel.range}")
        @Max(value = 5, message = "{validation.reaction.energyLevel.range}")
        Integer energyLevel,

        boolean hasHeartburn,
        boolean hasGas,
        boolean hasBloating,
        boolean hasHeadache,
        String memo
) {
    public Reaction toEntity(Long mealId) {
        return Reaction.builder()
                .mealId(mealId)
                .digestionLevel(digestionLevel.shortValue())
                .fullnessLevel(fullnessLevel.shortValue())
                .energyLevel(energyLevel.shortValue())
                .hasHeartburn(Optional.ofNullable(hasHeartburn).orElse(false))
                .hasGas(Optional.ofNullable(hasGas).orElse(false))
                .hasBloating(Optional.ofNullable(hasBloating).orElse(false))
                .hasHeadache(Optional.ofNullable(hasHeadache).orElse(false))
                .memo(memo)
                .build();
    }
}
