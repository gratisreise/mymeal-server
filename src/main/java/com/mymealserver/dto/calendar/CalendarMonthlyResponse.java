package com.mymealserver.dto.calendar;

import java.util.Map;

public record CalendarMonthlyResponse(
        Integer year,
        Integer month,
        Map<String, DailySummaryResponse> days
) {
}
