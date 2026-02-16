package com.mymealserver.dto.profile;

import java.time.LocalTime;

public record SettingsResponse(
        NotificationsResponse notifications
) {
    public record NotificationsResponse(
            Boolean recommendationEnabled,
            Boolean reactionReminderEnabled,
            Boolean mealReminderEnabled,
            MealTimesResponse mealTimes
    ) {
    }

    public record MealTimesResponse(
            LocalTime breakfast,
            LocalTime lunch,
            LocalTime dinner
    ) {
    }
}
