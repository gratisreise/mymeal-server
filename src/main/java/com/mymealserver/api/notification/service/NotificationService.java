package com.mymealserver.api.notification.service;

import com.mymealserver.api.notification.dto.response.NotificationListResponse;
import com.mymealserver.api.notification.dto.response.NotificationResponse;
import com.mymealserver.common.response.PageResponse.Pagination;
import com.mymealserver.domain.notification.Notification;
import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public NotificationListResponse getNotifications(Long memberId, Boolean unreadOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationReader.findByMemberId(memberId, unreadOnly, pageable);
        Page<NotificationResponse> notificationPage = notifications.map(NotificationResponse::from);
        long unreadCount = notificationReader.countUnread(memberId);

        return NotificationListResponse.of(
                notificationPage.getContent(),
                Pagination.from(notificationPage),
                unreadCount
        );
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
