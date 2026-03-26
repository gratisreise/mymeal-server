package com.mymealserver.api.ranking.service;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

@Getter
public class DateRange {

  private final LocalDate startDate;
  private final LocalDate endDate;

  private DateRange(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static DateRange of(LocalDate startDate, LocalDate endDate) {
    return new DateRange(startDate, endDate);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DateRange dateRange = (DateRange) o;
    return Objects.equals(startDate, dateRange.startDate)
        && Objects.equals(endDate, dateRange.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startDate, endDate);
  }
}
