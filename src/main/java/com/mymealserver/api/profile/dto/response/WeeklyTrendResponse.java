package com.mymealserver.api.profile.dto.response;

import java.time.LocalDate;

public record WeeklyTrendResponse(
        LocalDate date,
        Double averageScore
) {
}
