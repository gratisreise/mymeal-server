package com.mymealserver.domain.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Member의 알림 목록 조회 (읽음 상태 필터링 지원)
    Page<Notification> findByMemberIdAndIsReadAndDeletedAtIsNull(
            Long memberId, Boolean isRead, Pageable pageable
    );

    // Member의 모든 알림 조회
    Page<Notification> findByMemberIdAndDeletedAtIsNull(
            Long memberId, Pageable pageable
    );

    // Member의 읽지 않은 알림 수 조회
    long countByMemberIdAndIsReadAndDeletedAtIsNull(
            Long memberId, boolean isRead
    );

    // 특정 알림 조회 (권한 검증용)
    Optional<Notification> findByIdAndMemberIdAndDeletedAtIsNull(
            Long notificationId, Long memberId
    );
}
