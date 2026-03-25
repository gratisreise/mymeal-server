package com.mymealserver.external.redis;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedNotificationService {

    private final StringRedisTemplate redisTemplate;

    private static final String NOTIFICATION_QUEUE_KEY = "notification:unified:queue";

    public void schedule(NotificationPayload payload, LocalDateTime notificationTime) {
        long score = notificationTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        String json = payload.toJson();

        redisTemplate.opsForZSet().add(NOTIFICATION_QUEUE_KEY, json, score);

        log.debug("알림 스케줄링 완료 - 회원ID: {}, 타입: {}, 발송예정시간: {}",
                payload.memberId(), payload.type(), notificationTime);
    }

    public Set<NotificationPayload> fetchDueNotifications(LocalDateTime now) {
        long nowScore = now.atZone(ZoneId.systemDefault()).toEpochSecond();

        Set<String> jsonSet = redisTemplate.opsForZSet()
                .rangeByScore(NOTIFICATION_QUEUE_KEY, 0, nowScore);

        if (jsonSet == null || jsonSet.isEmpty()) {
            return Set.of();
        }

        log.debug("발송 대상 알림 조회 완료 - 건수: {}", jsonSet.size());

        return jsonSet.stream()
                .map(NotificationPayload::fromJson)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void remove(NotificationPayload payload) {
        try {
            String json = payload.toJson();
            redisTemplate.opsForZSet().remove(NOTIFICATION_QUEUE_KEY, json);
            log.debug("알림 제거 완료 - 타입: {}, 회원ID: {}",
                    payload.type(), payload.memberId());
        } catch (Exception e) {
            log.error("알림 제거 실패 - 회원ID: {}", payload.memberId(), e);
        }
    }

}
