package com.mymealserver.api.notification.service;

import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import com.mymealserver.domain.notification.Notification;
import com.mymealserver.api.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationReader notificationReader;
    private final NotificationWriter notificationWriter;

    public Page<NotificationResponse> getNotifications(Long memberId, Boolean unreadOnly, Pageable pageable) {
        Page<Notification> notifications = notificationReader.findByMemberId(
                memberId, unreadOnly, pageable
        );
        return notifications.map(NotificationResponse::from);
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

    public long getUnreadCount(Long memberId) {
        return notificationReader.countUnread(memberId);
    }
}
