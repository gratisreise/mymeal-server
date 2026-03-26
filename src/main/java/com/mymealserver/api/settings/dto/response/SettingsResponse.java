package com.mymealserver.api.settings.dto.response;

import com.mymealserver.domain.membersettings.MemberSettings;
import java.time.LocalTime;

public record SettingsResponse(NotificationsResponse notifications) {

  public static SettingsResponse from(MemberSettings settings) {
    return new SettingsResponse(NotificationsResponse.from(settings));
  }

  public record NotificationsResponse(
      Boolean recommendationEnabled,
      Boolean reactionReminderEnabled,
      Boolean mealReminderEnabled,
      MealTimesResponse mealTimes) {

    public static NotificationsResponse from(MemberSettings settings) {
      return new NotificationsResponse(
          settings.getRecommendationEnabled(),
          settings.getReactionReminderEnabled(),
          settings.getMealReminderEnabled(),
          MealTimesResponse.from(settings));
    }
  }

  public record MealTimesResponse(LocalTime breakfast, LocalTime lunch, LocalTime dinner) {

    public static MealTimesResponse from(MemberSettings settings) {
      return new MealTimesResponse(
          settings.getBreakfastTime(), settings.getLunchTime(), settings.getDinnerTime());
    }
  }
}
