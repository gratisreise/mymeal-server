package com.mymealserver.dto.profile;

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
