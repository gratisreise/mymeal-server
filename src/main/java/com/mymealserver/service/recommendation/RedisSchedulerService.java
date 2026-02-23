package com.mymealserver.service.recommendation;

import com.mymealserver.domain.recommendation.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSchedulerService {

    private static final String RECOMMENDATION_NOTIFICATIONS_KEY = "meal:notifications";
    private static final int NOTIFICATION_ADVANCE_MINUTES = 30;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Schedule a recommendation notification in Redis Sorted Set
     * Score = scheduledTime - 30 minutes (send notification 30 min before meal time)
     *
     * @param recommendation The recommendation to schedule
     */
    public void scheduleNotification(Recommendation recommendation) {
        try {
            LocalDateTime scheduledTime = recommendation.getScheduledTime();
            LocalDateTime notificationTime = scheduledTime.minusMinutes(NOTIFICATION_ADVANCE_MINUTES);

            long score = notificationTime.atZone(ZoneId.systemDefault()).toEpochSecond();

            redisTemplate.opsForZSet().add(
                    RECOMMENDATION_NOTIFICATIONS_KEY,
                    recommendation.getId().toString(),
                    score
            );

            log.debug("Scheduled notification for recommendationId: {} at score: {} (scheduled for: {})",
                    recommendation.getId(), score, notificationTime);

        } catch (Exception e) {
            log.error("Failed to schedule notification for recommendationId: {}",
                    recommendation.getId(), e);
            throw e;
        }
    }

    /**
     * Schedule multiple recommendations in bulk
     *
     * @param recommendations List of recommendations to schedule
     */
    public void scheduleNotifications(Iterable<Recommendation> recommendations) {
        int count = 0;
        for (Recommendation recommendation : recommendations) {
            scheduleNotification(recommendation);
            count++;
        }
        log.info("Scheduled {} notifications in Redis", count);
    }

    /**
     * Remove a scheduled notification from Redis
     *
     * @param recommendationId The recommendation ID to remove
     */
    public void removeScheduledNotification(Long recommendationId) {
        redisTemplate.opsForZSet().remove(RECOMMENDATION_NOTIFICATIONS_KEY, recommendationId.toString());
        log.debug("Removed scheduled notification for recommendationId: {}", recommendationId);
    }

    /**
     * Get all recommendation IDs that are due for notification
     * (score <= current timestamp)
     *
     * @param currentTimestamp Current Unix timestamp
     * @return Set of recommendation IDs due for notification
     */
    public java.util.Set<String> getDueNotifications(long currentTimestamp) {
        return redisTemplate.opsForZSet()
                .rangeByScore(RECOMMENDATION_NOTIFICATIONS_KEY, 0, currentTimestamp);
    }

    /**
     * Get the count of scheduled notifications
     *
     * @return Number of pending notifications
     */
    public long getScheduledNotificationCount() {
        Long count = redisTemplate.opsForZSet().size(RECOMMENDATION_NOTIFICATIONS_KEY);
        return count != null ? count : 0;
    }

    /**
     * Clean up old scheduled notifications (for maintenance)
     *
     * @param beforeTimestamp Remove notifications scheduled before this timestamp
     */
    public void cleanupOldNotifications(long beforeTimestamp) {
        redisTemplate.opsForZSet().removeRangeByScore(RECOMMENDATION_NOTIFICATIONS_KEY, 0, beforeTimestamp);
        log.info("Cleaned up notifications scheduled before timestamp: {}", beforeTimestamp);
    }
}
