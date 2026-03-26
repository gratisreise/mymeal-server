package com.mymealserver.api.profile.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record BodyPatternResponse(
    List<BodyPatternTagResponse> goodTags,
    List<BodyPatternTagResponse> badTags,
    Double overallAverageScore) {

  public static BodyPatternResponse of(
      List<BodyPatternTagResponse> goodTags,
      List<BodyPatternTagResponse> badTags,
      Double overallAverageScore) {
    return BodyPatternResponse.builder()
        .goodTags(goodTags)
        .badTags(badTags)
        .overallAverageScore(overallAverageScore)
        .build();
  }
}
