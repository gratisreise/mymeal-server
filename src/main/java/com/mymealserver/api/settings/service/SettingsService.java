package com.mymealserver.api.settings.service;

import com.mymealserver.api.profile.dto.request.UpdateNotificationRequest;
import com.mymealserver.api.settings.dto.response.SettingsResponse;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsReader;
import com.mymealserver.domain.membersettings.MemberSettingsWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingsService {

  private final MemberSettingsReader memberSettingsReader;
  private final MemberSettingsWriter memberSettingsWriter;

  public SettingsResponse getSettings(Long memberId) {
    MemberSettings settings = memberSettingsReader.findByMemberId(memberId);
    return SettingsResponse.from(settings);
  }

  @Transactional
  public void updateNotificationSettings(Long memberId, UpdateNotificationRequest request) {

    MemberSettings settings = memberSettingsReader.findByMemberId(memberId);

    // 추천 알림 설정 업데이트
    if (request.recommendationEnabled() != null) {
      if (request.recommendationEnabled()) {
        settings.enableRecommendation();
      } else {
        settings.disableRecommendation();
      }
    }

    // 반응 리마인더 알림 설정 업데이트
    if (request.reactionReminderEnabled() != null) {
      if (request.reactionReminderEnabled()) {
        settings.enableReactionReminder();
      } else {
        settings.disableReactionReminder();
      }
    }

    // 식사 리마인더 알림 설정 업데이트
    if (request.mealReminderEnabled() != null) {
      if (request.mealReminderEnabled()) {
        settings.enableMealReminder();
      } else {
        settings.disableMealReminder();
      }
    }

    // 식사 시간 설정 업데이트
    if (request.mealTimes() != null) {
      UpdateNotificationRequest.MealTimesData mealTimes = request.mealTimes();

      if (mealTimes.breakfast() != null) {
        settings.setBreakfastTime(mealTimes.breakfast());
      }

      if (mealTimes.lunch() != null) {
        settings.setLunchTime(mealTimes.lunch());
      }

      if (mealTimes.dinner() != null) {
        settings.setDinnerTime(mealTimes.dinner());
      }
    }

    memberSettingsWriter.save(settings);
  }
}
