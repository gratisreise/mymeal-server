package com.mymealserver.api.notification.service;

import com.mymealserver.api.notification.dto.response.NotificationListResponse;
import com.mymealserver.api.notification.dto.response.NotificationResponse;
import com.mymealserver.api.notification.dto.response.UnreadCountResponse;
import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.notification.Notification;
import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

  private final NotificationReader notificationReader;
  private final NotificationWriter notificationWriter;

  public NotificationListResponse getNotifications(
      Long memberId, Long cursor, int size, NotificationType type) {
    Slice<Notification> slice =
        notificationReader.findByMemberIdWithCursor(
            memberId, cursor, type, PageRequest.of(0, size + 1));

    List<Notification> content = slice.getContent();
    boolean hasNext = content.size() > size;
    List<Notification> trimmed = hasNext ? content.subList(0, size) : content;
    Long nextCursor = hasNext ? trimmed.get(trimmed.size() - 1).getId() : null;

    List<NotificationResponse> responses =
        trimmed.stream().map(NotificationResponse::from).toList();
    long unreadCount = notificationReader.countUnread(memberId);

    return NotificationListResponse.of(responses, nextCursor, hasNext, size, unreadCount);
  }

  @Transactional
  public void markAsRead(Long memberId, Long notificationId) {
    Notification notification = notificationReader.findByIdAndMemberId(notificationId, memberId);
    notificationWriter.markAsRead(notification);
  }

  @Transactional
  public void markAllAsRead(Long memberId) {
    notificationWriter.markAllAsRead(memberId);
  }

  @Transactional
  public void markAsReadBatch(Long memberId, List<Long> ids) {
    List<Notification> notifications = notificationReader.findByIdsAndMemberId(ids, memberId);
    if (notifications.size() != ids.size()) {
      throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
    notificationWriter.markAsReadBatch(notifications);
  }

  @Transactional
  public void deleteNotification(Long memberId, Long notificationId) {
    Notification notification = notificationReader.findByIdAndMemberId(notificationId, memberId);
    notificationWriter.softDelete(notification);
  }

  public UnreadCountResponse getUnreadCount(Long memberId) {
    return UnreadCountResponse.of(notificationReader.countUnread(memberId));
  }
}
