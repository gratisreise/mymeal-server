package com.mymealserver.api.profile.dto.response;

import lombok.Builder;

@Builder
public record BodyPatternTagResponse(String tag, Double averageScore, Integer count) {

  public static BodyPatternTagResponse of(String tag, Double averageScore, Integer count) {
    return BodyPatternTagResponse.builder()
        .tag(tag)
        .averageScore(averageScore)
        .count(count)
        .build();
  }
}
