package com.mymealserver.dto.profile;

import java.util.List;

public record StatisticsResponse(
        Integer totalMealCount,
        Double reactionRate,
        List<TagCountResponse> topTags,
        List<WeeklyTrendResponse> weeklyTrend
) {
}
