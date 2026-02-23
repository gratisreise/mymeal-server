package com.mymealserver.external.fcm.service.scheduler;

import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.external.redis.service.ReactionNotificationQueueService;
import com.mymealserver.external.fcm.service.FcmNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionNotificationScheduler {

    private final ReactionNotificationQueueService queueService;
    private final MealReader mealReader;
    private final MealWriter mealWriter;
    private final FcmNotificationService fcmNotificationService;

    /**
     * 식후 반응 알림 스케줄러
     * 1분마다 실행하여 도래한 알림을 발송
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void processPendingNotifications() {
        try {
            // 1. 도래한 알림 조회
            Set<Object> dueNotifications = queueService.fetchDueNotifications(LocalDateTime.now());

            if (dueNotifications == null || dueNotifications.isEmpty()) {
                return;
            }

            log.info("Processing {} due reaction notifications", dueNotifications.size());

            // 2. 각 알림 처리
            for (Object notification : dueNotifications) {
                Long mealId = Long.valueOf(notification.toString());

                try {
                    // 3. DB 상태 검증
                    Meal meal = mealReader.findByIdOptional(mealId).orElse(null);

                    // Meal이 삭제되었으면 알림 큐에서 제거하고 건너뜀
                    if (meal == null || meal.isDeleted()) {
                        log.debug("Meal {} not found or deleted, skipping notification", mealId);
                        queueService.removeNotification(mealId);
                        continue;
                    }

                    // 이미 알림을 발송했으면 건너뜀
                    if (meal.isNotificationSent()) {
                        log.debug("Notification already sent for meal {}", mealId);
                        queueService.removeNotification(mealId);
                        continue;
                    }

                    // 4. FCM 발송 (비동기)
                    fcmNotificationService.sendReactionReminder(meal.getMemberId(), mealId);

                    // 5. DB에 notificationSent=true 업데이트
                    meal.markNotificationSent();
                    mealWriter.save(meal);

                    // 6. 알림 큐에서 제거
                    queueService.removeNotification(mealId);

                    log.info("Reaction notification sent for meal: {}", mealId);

                } catch (Exception e) {
                    log.error("Error processing reaction notification for meal: {}", mealId, e);
                    // 실패 시에도 큐에서 제거하여 재시도 무한 루프 방지
                    queueService.removeNotification(mealId);
                }
            }

        } catch (Exception e) {
            log.error("Error in reaction notification scheduler", e);
        }
    }
}
