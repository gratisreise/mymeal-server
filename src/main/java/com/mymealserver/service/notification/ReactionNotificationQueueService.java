package com.mymealserver.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionNotificationQueueService {

    private static final String NOTIFICATION_QUEUE_KEY = "meal:reaction:notifications";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 식후 반응 알림 예약
     *
     * @param mealId            식사 ID
     * @param notificationTime 알림 발송 시간
     */
    public void scheduleReactionNotification(Long mealId, LocalDateTime notificationTime) {
        long score = notificationTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        redisTemplate.opsForZSet().add(NOTIFICATION_QUEUE_KEY, mealId, score);
        log.debug("Scheduled reaction notification for mealId: {} at {}", mealId, notificationTime);
    }

    /**
     * 도래한 알림 조회 (현재 시간 이전의 알림)
     *
     * @param now 현재 시간
     * @return 도래한 mealId 목록
     */
    public Set<Object> fetchDueNotifications(LocalDateTime now) {
        long nowScore = now.atZone(ZoneId.systemDefault()).toEpochSecond();
        Set<Object> notifications = redisTemplate.opsForZSet().rangeByScore(NOTIFICATION_QUEUE_KEY, 0, nowScore);
        log.debug("Fetched {} due notifications", notifications != null ? notifications.size() : 0);
        return notifications;
    }

    /**
     * 알림 큐에서 제거
     *
     * @param mealId 식사 ID
     */
    public void removeNotification(Long mealId) {
        redisTemplate.opsForZSet().remove(NOTIFICATION_QUEUE_KEY, mealId);
        log.debug("Removed reaction notification for mealId: {}", mealId);
    }

    /**
     * 알림 큐 크기 조회 (모니터링용)
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(NOTIFICATION_QUEUE_KEY);
        return size != null ? size : 0;
    }
}
