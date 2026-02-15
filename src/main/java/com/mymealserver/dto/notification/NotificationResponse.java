package com.mymealserver.dto.notification;

import com.mymealserver.entity.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        Map<String, Object> data,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
