package com.mymealserver.domain.notification;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReader {

    private final NotificationRepository notificationRepository;

    public Page<Notification> findByMemberId(Long memberId, Boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return notificationRepository.findByMemberIdAndIsReadAndDeletedAtIsNull(
                    memberId, false, pageable
            );
        }
        return notificationRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);
    }

    public Notification findByIdAndMemberId(Long notificationId, Long memberId) {
        return notificationRepository.findByIdAndMemberIdAndDeletedAtIsNull(notificationId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    public long countUnread(Long memberId) {
        return notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(memberId, false);
    }
}
