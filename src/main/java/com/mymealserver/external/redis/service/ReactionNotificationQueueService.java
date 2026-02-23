package com.mymealserver.external.redis.service;

import com.mymealserver.external.redis.key.RedisKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

/**
 * 식후 반응 알림 큐 서비스
 *
 * <p>Redis Sorted Set을 사용하여 식후 반응 알림을 스케줄링합니다.
 * 식사 기록 후 일정 시간이 지나면 사용자에게 반응 기록을 알립니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionNotificationQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyProvider keyProvider;

    /**
     * 식후 반응 알림 예약
     *
     * @param mealId            식사 ID
     * @param notificationTime 알림 발송 시간
     */
    public void scheduleReactionNotification(Long mealId, LocalDateTime notificationTime) {
        long score = notificationTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        redisTemplate.opsForZSet().add(keyProvider.reactionNotificationKey(), mealId, score);
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
        Set<Object> notifications = redisTemplate.opsForZSet()
                .rangeByScore(keyProvider.reactionNotificationKey(), 0, nowScore);
        log.debug("Fetched {} due notifications", notifications != null ? notifications.size() : 0);
        return notifications;
    }

    /**
     * 알림 큐에서 제거
     *
     * @param mealId 식사 ID
     */
    public void removeNotification(Long mealId) {
        redisTemplate.opsForZSet().remove(keyProvider.reactionNotificationKey(), mealId);
        log.debug("Removed reaction notification for mealId: {}", mealId);
    }

    /**
     * 알림 큐 크기 조회 (모니터링용)
     *
     * @return 대기 중인 알림 수
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(keyProvider.reactionNotificationKey());
        return size != null ? size : 0;
    }
}
