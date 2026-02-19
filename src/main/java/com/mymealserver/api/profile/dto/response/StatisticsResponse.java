package com.mymealserver.api.profile.dto.response;

import java.util.List;

public record StatisticsResponse(
        Integer totalMealCount,
        Double reactionRate,
        List<TagCountResponse> topTags,
        List<WeeklyTrendResponse> weeklyTrend
) {
}
