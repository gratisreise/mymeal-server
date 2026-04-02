package com.mymealserver.domain.notification;

import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReader {

  private final NotificationRepository notificationRepository;

  public Slice<Notification> findByMemberIdWithCursor(
      Long memberId, Long cursor, NotificationType type, Pageable pageable) {
    if (type != null) {
      return notificationRepository.findByMemberIdAndTypeWithCursor(
          memberId, type, cursor, pageable);
    }
    return notificationRepository.findByMemberIdWithCursor(memberId, cursor, pageable);
  }

  public long countUnread(Long memberId) {
    return notificationRepository.countByMemberIdAndIsReadAndDeletedAtIsNull(memberId, false);
  }

  public Notification findByIdAndMemberId(Long notificationId, Long memberId) {
    return notificationRepository
        .findByIdAndMemberIdAndDeletedAtIsNull(notificationId, memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
  }

  public List<Notification> findByIdsAndMemberId(List<Long> ids, Long memberId) {
    return notificationRepository.findAllByIdInAndMemberIdAndDeletedAtIsNull(ids, memberId);
  }
}