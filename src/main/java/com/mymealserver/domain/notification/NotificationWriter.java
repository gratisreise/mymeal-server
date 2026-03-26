package com.mymealserver.domain.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.external.redis.NotificationPayload;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationWriter {

  private final NotificationRepository notificationRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public Notification save(Notification notification) {
    return notificationRepository.save(notification);
  }

  @Transactional
  public Notification saveFromPayload(NotificationPayload payload) {
    String dataJson = toJson(payload.data());

    Notification notification =
        Notification.builder()
            .memberId(payload.memberId())
            .type(payload.type())
            .title(payload.title())
            .body(payload.body())
            .data(dataJson)
            .sentAt(LocalDateTime.now())
            .build();

    return notificationRepository.save(notification);
  }

  private String toJson(Map<String, String> data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (Exception e) {
      return "{}";
    }
  }

  @Transactional
  public void markAsRead(Notification notification) {
    notification.markAsRead();
    notificationRepository.save(notification);
  }

  @Transactional
  public void markAllAsRead(Long memberId) {
    List<Notification> unreadNotifications =
        notificationRepository
            .findByMemberIdAndIsReadAndDeletedAtIsNull(
                memberId, false, org.springframework.data.domain.Pageable.unpaged())
            .getContent();

    unreadNotifications.forEach(Notification::markAsRead);
    notificationRepository.saveAll(unreadNotifications);
  }
}
