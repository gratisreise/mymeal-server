package com.mymealserver.dto.reaction;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(

        @NotNull(message = "소화 상태는 필수입니다.")
        @Min(value = 1, message = "소화 상태는 1~5 사이여야 합니다.")
        @Max(value = 5, message = "소화 상태는 1~5 사이여야 합니다.")
        Integer digestionLevel,

        @NotNull(message = "포만감은 필수입니다.")
        @Min(value = 1, message = "포만감은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "포만감은 1~5 사이여야 합니다.")
        Integer fullnessLevel,

        @NotNull(message = "에너지 레벨은 필수입니다.")
        @Min(value = 1, message = "에너지 레벨은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "에너지 레벨은 1~5 사이여야 합니다.")
        Integer energyLevel,

        Boolean hasHeartburn,
        Boolean hasGas,
        Boolean hasBloating,
        Boolean hasHeadache,
        String memo
) {
}
