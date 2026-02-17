package com.mymealserver.domain.notification;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.Notification;
import com.mymealserver.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReader {

    private final NotificationRepository notificationRepository;

    /**
     * 페이지네이션된 알림 목록 조회 (unreadOnly 필터 지원)
     */
    public Page<Notification> findByMemberId(Long memberId, Boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    memberId, false, pageable
            );
        }
        return notificationRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);
    }

    /**
     * 특정 알림 조회 (권한 검증용)
     */
    public Notification findByIdAndMemberId(Long notificationId, Long memberId) {
        return notificationRepository.findByIdAndMemberIdAndDeletedAtIsNull(notificationId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long countUnread(Long memberId) {
        return notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(memberId, false);
    }
}
