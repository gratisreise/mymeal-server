package com.mymealserver.domain.notification;

import com.mymealserver.common.enums.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Member의 알림 목록 조회 (읽음 상태 필터링 지원)
  Page<Notification> findByMemberIdAndIsReadAndDeletedAtIsNull(
      Long memberId, Boolean isRead, Pageable pageable);

  // Member의 모든 알림 조회
  Page<Notification> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

  // Member의 읽지 않은 알림 수 조회
  long countByMemberIdAndIsReadAndDeletedAtIsNull(Long memberId, boolean isRead);

  // 특정 알림 조회 (권한 검증용)
  Optional<Notification> findByIdAndMemberIdAndDeletedAtIsNull(Long notificationId, Long memberId);

  // 커서 기반 알림 목록 조회 (전체)
  @Query(
      "SELECT n FROM Notification n WHERE n.memberId = :memberId "
          + "AND n.deletedAt IS NULL AND (:cursor IS NULL OR n.id < :cursor) "
          + "ORDER BY n.id DESC")
  Slice<Notification> findByMemberIdWithCursor(
      @Param("memberId") Long memberId,
      @Param("cursor") Long cursor,
 Pageable pageable);

  // 커서 기반 알림 목록 조회 (타입 필터링)
  @Query(
      "SELECT n FROM Notification n WHERE n.memberId = :memberId "
          + "AND n.type = :type AND n.deletedAt IS NULL "
          + "AND (:cursor IS NULL OR n.id < :cursor) "
          + "ORDER BY n.id DESC")
  Slice<Notification> findByMemberIdAndTypeWithCursor(
      @Param("memberId") Long memberId,
      @Param("type") NotificationType type,
      @Param("cursor") Long cursor, Pageable pageable);

  // 다중 읽음 처리용 (소유권 검증)
  List<Notification> findAllByIdInAndMemberIdAndDeletedAtIsNull(
      List<Long> ids, Long memberId);
}
