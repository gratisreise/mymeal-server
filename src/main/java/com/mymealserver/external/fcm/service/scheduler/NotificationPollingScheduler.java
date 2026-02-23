package com.mymealserver.external.fcm.service.scheduler;

import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.membersettings.MemberSettingsReader;
import com.mymealserver.domain.recommendation.RecommendationReader;
import com.mymealserver.domain.recommendation.RecommendationWriter;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.external.redis.service.RecommendationNotificationService;
import com.mymealserver.external.fcm.service.FcmNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPollingScheduler {

    private final RecommendationNotificationService recommendationNotificationService;
    private final RecommendationReader recommendationReader;
    private final RecommendationWriter recommendationWriter;
    private final MemberReader memberReader;
    private final MemberSettingsReader memberSettingsReader;
    private final FcmNotificationService fcmNotificationService;

    /**
     * Poll every minute for due notifications and send them
     * Runs every 60 seconds (60000 milliseconds)
     */
    @Scheduled(fixedRate = 60000)
    public void pollAndSendNotifications() {
        try {
            long currentTimestamp = System.currentTimeMillis() / 1000;

            // Get due notifications from Redis
            Set<String> dueRecommendationIds = recommendationNotificationService.getDueNotifications(currentTimestamp);

            if (dueRecommendationIds == null || dueRecommendationIds.isEmpty()) {
                log.debug("No due notifications found at timestamp: {}", currentTimestamp);
                return;
            }

            log.info("Found {} due notifications to send", dueRecommendationIds.size());

            int successCount = 0;
            int failureCount = 0;

            for (String recommendationIdStr : dueRecommendationIds) {
                try {
                    Long recommendationId = Long.valueOf(recommendationIdStr);

                    // Get recommendation from DB
                    Recommendation recommendation = recommendationReader.findById(recommendationId);

                    // Skip if already sent
                    if (recommendation.isNotificationSent()) {
                        log.warn("Recommendation {} already marked as sent, skipping", recommendationId);
                        recommendationNotificationService.removeScheduledNotification(recommendationId);
                        continue;
                    }

                    // Get member settings for FCM token
                    Member member = memberReader.findById(recommendation.getMemberId());
                    MemberSettings settings = memberSettingsReader.findByMemberIdOrNull(member.getId());

                    if (settings == null || settings.getFcmToken() == null) {
                        log.warn("No FCM token found for memberId: {}, skipping notification",
                                member.getId());
                        continue;
                    }

                    // Send FCM push notification
                    fcmNotificationService.sendRecommendationNotification(
                            settings.getFcmToken(),
                            recommendation.getPushMessage()
                    );

                    // Mark as sent
                    recommendationWriter.markAsSent(recommendation);

                    // Remove from Redis
                    recommendationNotificationService.removeScheduledNotification(recommendationId);

                    successCount++;
                    log.info("Successfully sent notification for recommendationId: {}", recommendationId);

                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to send notification for recommendationId: {}",
                            recommendationIdStr, e);
                }
            }

            log.info("Notification polling completed. Success: {}, Failure: {}",
                    successCount, failureCount);

        } catch (Exception e) {
            log.error("Error in notification polling scheduler", e);
        }
    }

    /**
     * Log the current number of scheduled notifications every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void logScheduledNotificationCount() {
        long count = recommendationNotificationService.getScheduledNotificationCount();
        log.info("Current scheduled notification count in Redis: {}", count);
    }
}
