package com.mymealserver.api.profile.dto.response;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record WeeklyTrendResponse(LocalDate date, Double averageScore) {

  public static WeeklyTrendResponse of(LocalDate date, Double averageScore) {
    return WeeklyTrendResponse.builder().date(date).averageScore(averageScore).build();
  }
}
