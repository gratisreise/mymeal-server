package com.mymealserver.api.calendar.dto;

import java.util.Map;

public record CalendarMonthlyResponse(
        Integer year,
        Integer month,
        Map<String, DailySummaryResponse> days
) {
}
