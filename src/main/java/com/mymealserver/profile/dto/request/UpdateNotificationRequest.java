package com.mymealserver.profile.dto.request;

import java.time.LocalTime;

public record UpdateNotificationRequest(
        Boolean recommendationEnabled,
        Boolean reactionReminderEnabled,
        Boolean mealReminderEnabled,
        MealTimesData mealTimes
) {
    public record MealTimesData(
                LocalTime breakfast,
                LocalTime lunch,
                LocalTime dinner
        ) {
        }
}
