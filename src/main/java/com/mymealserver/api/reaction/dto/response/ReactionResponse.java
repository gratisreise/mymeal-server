package com.mymealserver.api.reaction.dto.response;

import com.mymealserver.common.enums.GradeType;
import com.mymealserver.domain.reaction.Reaction;
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
    LocalDateTime createdAt) {
  public static ReactionResponse from(Reaction reaction) {
    if (reaction == null) {
      return null;
    }
    return new ReactionResponse(
        reaction.getId(),
        reaction.getMealId(),
        reaction.getDigestionLevel().intValue(),
        reaction.getFullnessLevel().intValue(),
        reaction.getEnergyLevel().intValue(),
        reaction.getHasHeartburn(),
        reaction.getHasGas(),
        reaction.getHasBloating(),
        reaction.getHasHeadache(),
        reaction.getMemo(),
        reaction.getOverallScore(),
        reaction.getGrade(),
        reaction.getCreatedAt());
  }
}
