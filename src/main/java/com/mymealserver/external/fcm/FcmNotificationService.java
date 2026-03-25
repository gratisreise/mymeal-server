package com.mymealserver.external.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsReader;
import com.mymealserver.domain.notification.NotificationWriter;
import com.mymealserver.external.redis.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final MemberSettingsReader memberSettingsReader;
    private final NotificationWriter notificationWriter;
    private final FirebaseMessaging firebaseMessaging;

    @Async("fcmNotificationExecutor")
    public void send(NotificationPayload payload) throws FirebaseMessagingException {
        MemberSettings memberSettings = memberSettingsReader.findByMemberIdOrNull(payload.memberId());

        if (memberSettings == null || !isNotificationEnabled(memberSettings, payload.type())) return;

        String fcmToken = memberSettings.getFcmToken();
        if (fcmToken == null || fcmToken.isBlank()) return;

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(payload.title())
                        .setBody(payload.body())
                        .build())
                .putAllData(payload.data())
                .build();
        firebaseMessaging.send(message);
        notificationWriter.saveFromPayload(payload);
    }

    private boolean isNotificationEnabled(MemberSettings settings, NotificationType type) {
        return switch (type) {
            case RECOMMENDATION -> settings.getRecommendationEnabled();
            case REACTION_REMINDER -> settings.getReactionReminderEnabled();
            case MEAL_REMINDER -> settings.getMealReminderEnabled();
        };
    }
}
