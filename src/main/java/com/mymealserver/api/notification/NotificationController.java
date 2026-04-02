package com.mymealserver.api.notification;

import com.mymealserver.api.notification.dto.request.BatchReadRequest;
import com.mymealserver.api.notification.dto.response.NotificationListResponse;
import com.mymealserver.api.notification.dto.response.UnreadCountResponse;
import com.mymealserver.api.notification.service.NotificationService;
import com.mymealserver.common.annotation.AuthenticatedMember;
import com.mymealserver.common.enums.NotificationType;
import com.mymealserver.common.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<SuccessResponse<NotificationListResponse>> getNotifications(
      @AuthenticatedMember Long memberId,
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) NotificationType type) {
    return SuccessResponse.toOk(notificationService.getNotifications(memberId, cursor, size, type));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<SuccessResponse<UnreadCountResponse>> getUnreadCount(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(notificationService.getUnreadCount(memberId));
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<SuccessResponse<Void>> markAsRead(
      @AuthenticatedMember Long memberId, @PathVariable Long id) {
    notificationService.markAsRead(memberId, id);
    return SuccessResponse.toNoContent();
  }

  @PutMapping("/read-all")
  public ResponseEntity<SuccessResponse<Void>> markAllAsRead(@AuthenticatedMember Long memberId) {
    notificationService.markAllAsRead(memberId);
    return SuccessResponse.toNoContent();
  }

  @PutMapping("/batch-read")
  public ResponseEntity<SuccessResponse<Void>> markAsReadBatch(
      @AuthenticatedMember Long memberId, @Valid @RequestBody BatchReadRequest request) {
    notificationService.markAsReadBatch(memberId, request.ids());
    return SuccessResponse.toNoContent();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<SuccessResponse<Void>> deleteNotification(
      @AuthenticatedMember Long memberId, @PathVariable Long id) {
    notificationService.deleteNotification(memberId, id);
    return SuccessResponse.toNoContent();
  }
}
