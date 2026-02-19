package com.mymealserver.api.reaction.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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

        Boolean hasHeartburn,
        Boolean hasGas,
        Boolean hasBloating,
        Boolean hasHeadache,
        String memo
) {
}
