package com.mymealserver.api.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.notification.NotificationWriter;
import com.mymealserver.domain.member.MemberSettingsReader;
import com.mymealserver.entity.MemberSettings;
import com.mymealserver.entity.Notification;
import com.mymealserver.entity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final MemberSettingsReader memberSettingsReader;
    private final NotificationWriter notificationWriter;
    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    /**
     * 식후 반응 기록 알림 발송 (비동기)
     *
     * @param memberId 회원 ID
     * @param mealId   식사 ID
     */
    @Async("fcmNotificationExecutor")
    public void sendReactionReminder(Long memberId, Long mealId) {
        try {
            // 1. MemberSettings에서 FCM 토큰 조회
            MemberSettings memberSettings = memberSettingsReader.findByMemberId(memberId);

            // 반응 알림 비활성화 확인
            if (!memberSettings.getReactionReminderEnabled()) {
                log.info("Reaction reminder is disabled for member: {}", memberId);
                return;
            }

            String fcmToken = memberSettings.getFcmToken();
            if (fcmToken == null || fcmToken.isBlank()) {
                log.warn("FCM token not found for member: {}", memberId);
                return;
            }

            // 2. FCM 메시지 생성
            String title = "식후 반응 기록하기";
            String body = "식사 후 30분이 지났습니다. 현재 속쓰이는 건 어떠신가요?";

            // Data payload (앱에서 식사 상세 화면으로 이동용)
            Map<String, String> data = new HashMap<>();
            data.put("mealId", String.valueOf(mealId));
            data.put("redirectTo", "reaction");
            data.put("type", "REACTION_REMINDER");

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            // 3. FCM 발송
            String messageId = firebaseMessaging.send(message);
            log.info("FCM reaction reminder sent successfully. Member: {}, Meal: {}, MessageId: {}",
                    memberId, mealId, messageId);

            // 4. Notification 테이블에 기록
            String dataJson;
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                dataJson = "{}";
            }

            Notification notification = Notification.builder()
                    .memberId(memberId)
                    .type(NotificationType.REACTION_REMINDER)
                    .title(title)
                    .body(body)
                    .data(dataJson)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

            notificationWriter.save(notification);

        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for member: {}, meal: {}", memberId, mealId, e);

            // 토큰 만료 등의 경우 (INVALID_ARGUMENT, UNREGISTERED)
            if (e.getMessagingErrorCode() != null) {
                log.warn("FCM token error: {}", e.getMessagingErrorCode());
                // 토큰 무효화 로직이 필요하면 여기에 추가
            }
        } catch (Exception e) {
            log.error("Unexpected error sending FCM for member: {}, meal: {}", memberId, mealId, e);
        }
    }

    /**
     * 식단 추천 알림 발송
     *
     * @param fcmToken FCM 토큰
     * @param message  추천 메시지
     */
    public void sendRecommendationNotification(String fcmToken, String message) {
        try {
            // FCM 메시지 생성
            String title = "🍽️ 오늘의 식단 추천";

            // Data payload
            Map<String, String> data = new HashMap<>();
            data.put("redirectTo", "recommendations");
            data.put("type", "RECOMMENDATION");

            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .putAllData(data)
                    .build();

            // FCM 발송
            String messageId = firebaseMessaging.send(fcmMessage);
            log.info("FCM recommendation notification sent successfully. MessageId: {}", messageId);

        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for recommendation notification", e);

            // 토큰 만료 등의 경우
            if (e.getMessagingErrorCode() != null) {
                log.warn("FCM token error: {}", e.getMessagingErrorCode());
                throw new BusinessException(ErrorCode.NOTIFICATION_FCM_TOKEN_INVALID);
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error sending FCM recommendation notification", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
