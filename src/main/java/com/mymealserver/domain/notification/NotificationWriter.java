package com.mymealserver.domain.notification;

import com.mymealserver.entity.Notification;
import com.mymealserver.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationWriter {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 저장
     */
    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * 알림 읽음 상태 변경 (단일)
     */
    @Transactional
    public void markAsRead(Notification notification) {
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 회원의 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long memberId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByMemberIdAndIsReadAndDeletedAtIsNull(memberId, false, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }
}
