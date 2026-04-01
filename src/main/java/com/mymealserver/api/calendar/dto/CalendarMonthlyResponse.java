package com.mymealserver.api.calendar.dto;

import java.time.LocalDate;
import java.util.Map;

public record CalendarMonthlyResponse(
    Integer year, Integer month, Map<LocalDate, DailySummaryResponse> days) {}
