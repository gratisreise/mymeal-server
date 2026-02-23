package com.mymealserver.external.redis.service;

import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.external.redis.key.RedisKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 식사 추천 알림 스케줄링 서비스
 *
 * <p>Redis Sorted Set을 사용하여 추천 알림을 스케줄링합니다.
 * 식사 시간 30분 전에 알림을 발송하도록 스코어를 설정합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationNotificationService {

    private static final int NOTIFICATION_ADVANCE_MINUTES = 30;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyProvider keyProvider;

    /**
     * 추천 알림을 Redis Sorted Set에 스케줄링
     * Score = scheduledTime - 30 minutes (식사 시간 30분 전에 알림 발송)
     *
     * @param recommendation 스케줄링할 추천
     */
    public void scheduleNotification(Recommendation recommendation) {
        try {
            LocalDateTime scheduledTime = recommendation.getScheduledTime();
            LocalDateTime notificationTime = scheduledTime.minusMinutes(NOTIFICATION_ADVANCE_MINUTES);

            long score = notificationTime.atZone(ZoneId.systemDefault()).toEpochSecond();

            redisTemplate.opsForZSet().add(
                    keyProvider.recommendationNotificationKey(),
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
     * 여러 추천을 일괄 스케줄링
     *
     * @param recommendations 스케줄링할 추천 목록
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
     * 스케줄된 알림을 Redis에서 제거
     *
     * @param recommendationId 제거할 추천 ID
     */
    public void removeScheduledNotification(Long recommendationId) {
        redisTemplate.opsForZSet().remove(
                keyProvider.recommendationNotificationKey(),
                recommendationId.toString()
        );
        log.debug("Removed scheduled notification for recommendationId: {}", recommendationId);
    }

    /**
     * 알림 발송 시간이 도래한 추천 ID 목록 조회
     * (score <= current timestamp)
     *
     * @param currentTimestamp 현재 Unix 타임스탬프
     * @return 알림 발송 시간이 도래한 추천 ID 집합
     */
    public java.util.Set<String> getDueNotifications(long currentTimestamp) {
        return redisTemplate.opsForZSet()
                .rangeByScore(keyProvider.recommendationNotificationKey(), 0, currentTimestamp);
    }

    /**
     * 스케줄된 알림 수 조회
     *
     * @return 대기 중인 알림 수
     */
    public long getScheduledNotificationCount() {
        Long count = redisTemplate.opsForZSet().size(keyProvider.recommendationNotificationKey());
        return count != null ? count : 0;
    }

    /**
     * 오래된 스케줄된 알림 정리 (유지보수용)
     *
     * @param beforeTimestamp 이 타임스탬프 이전에 스케줄된 알림 제거
     */
    public void cleanupOldNotifications(long beforeTimestamp) {
        redisTemplate.opsForZSet().removeRangeByScore(
                keyProvider.recommendationNotificationKey(),
                0,
                beforeTimestamp
        );
        log.info("Cleaned up notifications scheduled before timestamp: {}", beforeTimestamp);
    }
}
