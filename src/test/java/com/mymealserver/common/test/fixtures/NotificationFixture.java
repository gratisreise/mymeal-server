package com.mymealserver.common.test.fixtures;

import com.mymealserver.domain.notification.Notification;
import com.mymealserver.common.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Test fixture for Notification entities
 * Provides reusable Notification instances for testing
 */
public class NotificationFixture {

    /**
     * Creates a default notification (unread)
     */
    public static Notification createDefaultNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.RECOMMENDATION)
                .title("식사 추천")
                .body("오늘 점심으로 샐러드를 추천합니다.")
                .data("{\"mealId\": 123}")
                .isRead(false)
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an unread notification
     */
    public static Notification createUnreadNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.RECOMMENDATION)
                .title("새로운 식사 추천")
                .body("건강한 식사를 추천합니다.")
                .data(null)
                .isRead(false)
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a read notification
     */
    public static Notification createReadNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.REACTION_REMINDER)
                .title("리액션 알림")
                .body("식사 후 리액션을 기록해주세요.")
                .data("{\"mealId\": 456}")
                .isRead(true)
                .readAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now().minusMinutes(30))
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a RECOMMENDATION type notification
     */
    public static Notification createRecommendationNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.RECOMMENDATION)
                .title("식사 추천 도착")
                .body("오늘의 추천 식사가 도착했습니다.")
                .data("{\"recommendationId\": 789}")
                .isRead(false)
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a REACTION_REMINDER type notification
     */
    public static Notification createReactionReminderNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.REACTION_REMINDER)
                .title("리액션 기록 알림")
                .body("식사 후 어떻게 느끼셨나요? 리액션을 기록해주세요.")
                .data("{\"mealId\": 101}")
                .isRead(false)
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a MEAL_REMINDER type notification
     */
    public static Notification createMealReminderNotification(Long id, Long memberId) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(NotificationType.MEAL_REMINDER)
                .title("식사 시간 알림")
                .body("점심 식사 시간입니다!")
                .data(null)
                .isRead(false)
                .readAt(null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a custom notification with specified fields
     */
    public static Notification createCustomNotification(
            Long id,
            Long memberId,
            NotificationType type,
            String title,
            String body,
            Boolean isRead
    ) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(type)
                .title(title)
                .body(body)
                .data(null)
                .isRead(isRead)
                .readAt(isRead ? LocalDateTime.now() : null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a custom notification with data field
     */
    public static Notification createCustomNotificationWithData(
            Long id,
            Long memberId,
            NotificationType type,
            String title,
            String body,
            String data,
            Boolean isRead
    ) {
        return Notification.builder()
                .id(id)
                .memberId(memberId)
                .type(type)
                .title(title)
                .body(body)
                .data(data)
                .isRead(isRead)
                .readAt(isRead ? LocalDateTime.now() : null)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
