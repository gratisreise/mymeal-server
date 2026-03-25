package com.mymealserver.external.fcm;

import com.mymealserver.external.redis.NotificationPayload;
import com.mymealserver.external.redis.UnifiedNotificationService;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPollingScheduler {

    private final UnifiedNotificationService unifiedNotificationService;
    private final FcmNotificationService fcmNotificationService;

    @Scheduled(fixedRate = 60000)
    public void pollAndSendNotifications() {
        Set<NotificationPayload> dueNotifications = unifiedNotificationService
                .fetchDueNotifications(LocalDateTime.now());

        if (dueNotifications.isEmpty()) {
            return;
        }

        for (NotificationPayload payload : dueNotifications) {
            try {
                fcmNotificationService.send(payload);
            } catch (Exception e) {
                log.error("Failed to send notification for memberId: {}", payload.memberId(), e);
            } finally {
                unifiedNotificationService.remove(payload);
            }
        }
    }
}
