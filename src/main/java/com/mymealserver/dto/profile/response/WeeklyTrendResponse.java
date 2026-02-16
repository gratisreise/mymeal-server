package com.mymealserver.dto.profile;

import java.time.LocalDate;

public record WeeklyTrendResponse(
        LocalDate date,
        Double averageScore
) {
}
