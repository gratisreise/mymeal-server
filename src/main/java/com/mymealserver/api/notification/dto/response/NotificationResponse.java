package com.mymealserver.api.notification.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String body,
    Map<String, Object> data,
    Boolean isRead,
    LocalDateTime createdAt) {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static NotificationResponse from(Notification notification) {
    Map<String, Object> dataMap = null;
    if (notification.getData() != null && !notification.getData().isBlank()) {
      try {
        dataMap = objectMapper.readValue(notification.getData(), new TypeReference<>() {});
      } catch (Exception e) {
        log.warn("Failed to parse notification data: {}", notification.getData(), e);
        dataMap = Map.of();
      }
    }

    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getBody(),
        dataMap,
        notification.getIsRead(),
        notification.getCreatedAt());
  }
}
