package com.mymealserver.api.profile.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record StatisticsResponse(
    Integer totalMealCount,
    Double reactionRate,
    List<TagCountResponse> topTags,
    List<WeeklyTrendResponse> weeklyTrend) {

  public static StatisticsResponse of(
      Integer totalMealCount,
      Double reactionRate,
      List<TagCountResponse> topTags,
      List<WeeklyTrendResponse> weeklyTrend) {
    return StatisticsResponse.builder()
        .totalMealCount(totalMealCount)
        .reactionRate(reactionRate)
        .topTags(topTags)
        .weeklyTrend(weeklyTrend)
        .build();
  }
}
