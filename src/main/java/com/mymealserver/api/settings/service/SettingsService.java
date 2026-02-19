package com.mymealserver.api.settings.service;

import com.mymealserver.api.profile.dto.request.UpdateNotificationRequest;
import com.mymealserver.api.settings.dto.response.SettingsResponse;
import com.mymealserver.domain.member.MemberSettingsReader;
import com.mymealserver.domain.member.MemberSettingsWriter;
import com.mymealserver.entity.MemberSettings;
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

    /**
     * Get user settings
     * Returns notification preferences and meal times
     */
    public SettingsResponse getSettings(Long memberId) {
        log.debug("Getting settings for memberId: {}", memberId);

        MemberSettings settings = memberSettingsReader.findByMemberId(memberId);

        return new SettingsResponse(
                new SettingsResponse.NotificationsResponse(
                        settings.getRecommendationEnabled(),
                        settings.getReactionReminderEnabled(),
                        settings.getMealReminderEnabled(),
                        new SettingsResponse.MealTimesResponse(
                                settings.getBreakfastTime(),
                                settings.getLunchTime(),
                                settings.getDinnerTime()
                        )
                )
        );
    }

    /**
     * Update notification settings
     * Only updates fields that are provided (non-null)
     * Meal times are updated if mealTimes object is provided
     */
    @Transactional
    public void updateNotificationSettings(Long memberId, UpdateNotificationRequest request) {
        log.debug("Updating notification settings for memberId: {}", memberId);

        MemberSettings settings = memberSettingsReader.findByMemberId(memberId);

        // Update notification flags if provided
        if (request.recommendationEnabled() != null) {
            if (request.recommendationEnabled()) {
                settings.enableRecommendation();
            } else {
                settings.disableRecommendation();
            }
            log.debug("Updated recommendationEnabled to: {}", request.recommendationEnabled());
        }

        if (request.reactionReminderEnabled() != null) {
            if (request.reactionReminderEnabled()) {
                settings.enableReactionReminder();
            } else {
                settings.disableReactionReminder();
            }
            log.debug("Updated reactionReminderEnabled to: {}", request.reactionReminderEnabled());
        }

        if (request.mealReminderEnabled() != null) {
            if (request.mealReminderEnabled()) {
                settings.enableMealReminder();
            } else {
                settings.disableMealReminder();
            }
            log.debug("Updated mealReminderEnabled to: {}", request.mealReminderEnabled());
        }

        // Update meal times if provided
        if (request.mealTimes() != null) {
            UpdateNotificationRequest.MealTimesData mealTimes = request.mealTimes();

            if (mealTimes.breakfast() != null) {
                settings.setBreakfastTime(mealTimes.breakfast());
                log.debug("Updated breakfastTime to: {}", mealTimes.breakfast());
            }

            if (mealTimes.lunch() != null) {
                settings.setLunchTime(mealTimes.lunch());
                log.debug("Updated lunchTime to: {}", mealTimes.lunch());
            }

            if (mealTimes.dinner() != null) {
                settings.setDinnerTime(mealTimes.dinner());
                log.debug("Updated dinnerTime to: {}", mealTimes.dinner());
            }
        }

        // Save updated settings
        memberSettingsWriter.save(settings);
        log.info("Updated notification settings for memberId: {}", memberId);
    }
}
