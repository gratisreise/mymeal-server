package com.mymealserver.api.notification;

import com.mymealserver.api.notification.dto.response.NotificationListResponse;
import com.mymealserver.api.notification.dto.response.NotificationResponse;
import com.mymealserver.api.notification.service.NotificationService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.PageResponse.Pagination;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<SuccessResponse<NotificationListResponse>> getNotifications(
            @CurrentMember Long memberId,
            @RequestParam(defaultValue = "false") Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notificationPage = notificationService.getNotifications(memberId, unreadOnly, pageable);
        long unreadCount = notificationService.getUnreadCount(memberId);

        NotificationListResponse response = NotificationListResponse.of(
                notificationPage.getContent(),
                Pagination.from(notificationPage),
                unreadCount
        );

        return SuccessResponse.toOk(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<SuccessResponse<Void>> markAsRead(
            @CurrentMember Long memberId,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(memberId, id);
        return SuccessResponse.toOk(null);
    }

    @PutMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> markAllAsRead(
            @CurrentMember Long memberId
    ) {
        notificationService.markAllAsRead(memberId);
        return SuccessResponse.toOk(null);
    }
}
