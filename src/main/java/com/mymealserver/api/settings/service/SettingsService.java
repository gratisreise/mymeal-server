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
    log.debug("회원 설정 조회 시작: memberId={}", memberId);

    MemberSettings settings = memberSettingsReader.findByMemberId(memberId);

    log.debug("회원 설정 조회 완료: memberId={}", memberId);
    return SettingsResponse.from(settings);
  }

  @Transactional
  public void updateNotificationSettings(Long memberId, UpdateNotificationRequest request) {
    log.debug("알림 설정 업데이트 시작: memberId={}", memberId);

    MemberSettings settings = memberSettingsReader.findByMemberId(memberId);

    // 추천 알림 설정 업데이트
    if (request.recommendationEnabled() != null) {
      if (request.recommendationEnabled()) {
        settings.enableRecommendation();
      } else {
        settings.disableRecommendation();
      }
      log.debug("추천 알림 설정 변경: recommendationEnabled={}", request.recommendationEnabled());
    }

    // 반응 리마인더 알림 설정 업데이트
    if (request.reactionReminderEnabled() != null) {
      if (request.reactionReminderEnabled()) {
        settings.enableReactionReminder();
      } else {
        settings.disableReactionReminder();
      }
      log.debug("반응 리마인더 설정 변경: reactionReminderEnabled={}", request.reactionReminderEnabled());
    }

    // 식사 리마인더 알림 설정 업데이트
    if (request.mealReminderEnabled() != null) {
      if (request.mealReminderEnabled()) {
        settings.enableMealReminder();
      } else {
        settings.disableMealReminder();
      }
      log.debug("식사 리마인더 설정 변경: mealReminderEnabled={}", request.mealReminderEnabled());
    }

    // 식사 시간 설정 업데이트
    if (request.mealTimes() != null) {
      UpdateNotificationRequest.MealTimesData mealTimes = request.mealTimes();

      if (mealTimes.breakfast() != null) {
        settings.setBreakfastTime(mealTimes.breakfast());
        log.debug("아침 식사 시간 변경: breakfastTime={}", mealTimes.breakfast());
      }

      if (mealTimes.lunch() != null) {
        settings.setLunchTime(mealTimes.lunch());
        log.debug("점심 식사 시간 변경: lunchTime={}", mealTimes.lunch());
      }

      if (mealTimes.dinner() != null) {
        settings.setDinnerTime(mealTimes.dinner());
        log.debug("저녁 식사 시간 변경: dinnerTime={}", mealTimes.dinner());
      }
    }

    memberSettingsWriter.save(settings);
    log.info("알림 설정 업데이트 완료: memberId={}", memberId);
  }
}
