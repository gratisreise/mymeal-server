package com.mymealserver.notification.controller;

import com.mymealserver.common.response.SuccessResponse;
import com.mymealserver.common.response.classes.Pagination;
import com.mymealserver.notification.dto.response.NotificationListResponse;
import com.mymealserver.notification.dto.response.NotificationResponse;
import com.mymealserver.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "페이지네이션된 알림 목록을 조회합니다.")
    public ResponseEntity<SuccessResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "false") Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationResponse> notifications = notificationService.getNotifications(
                memberId, unreadOnly, pageable
        );

        long unreadCount = notificationService.getUnreadCount(memberId);

        Pagination pagination = Pagination.from(notifications);

        NotificationListResponse response = NotificationListResponse.of(
                notifications.getContent(),
                pagination,
                unreadCount
        );

        return SuccessResponse.toOk(response);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<SuccessResponse<Void>> markAsRead(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(memberId, id);
        return SuccessResponse.toOk(null);
    }

    @PutMapping("/read-all")
    @Operation(summary = "전체 알림 읽음 처리", description = "현재 회원의 모든 알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<SuccessResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Long memberId
    ) {
        notificationService.markAllAsRead(memberId);
        return SuccessResponse.toOk(null);
    }
}
