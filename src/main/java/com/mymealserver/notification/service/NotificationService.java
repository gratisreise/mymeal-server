package com.mymealserver.notification.service;

import com.mymealserver.domain.notification.NotificationReader;
import com.mymealserver.domain.notification.NotificationWriter;
import com.mymealserver.entity.Notification;
import com.mymealserver.notification.dto.response.NotificationResponse;
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

    /**
     * 알림 목록 조회
     */
    public Page<NotificationResponse> getNotifications(Long memberId, Boolean unreadOnly, Pageable pageable) {
        Page<Notification> notifications = notificationReader.findByMemberId(
                memberId, unreadOnly, pageable
        );
        return notifications.map(NotificationResponse::from);
    }

    /**
     * 알림 단 건 읽음 처리
     */
    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationReader.findByIdAndMemberId(notificationId, memberId);
        notificationWriter.markAsRead(notification);
    }

    /**
     * 전체 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationWriter.markAllAsRead(memberId);
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long getUnreadCount(Long memberId) {
        return notificationReader.countUnread(memberId);
    }
}
