package com.mymealserver.api.notification.dto.response;

import java.util.List;

public record NotificationListResponse(
    List<NotificationResponse> data,
    Long nextCursor,
    boolean hasNext,
    int size,
    Integer unreadCount) {

  public static NotificationListResponse of(
      List<NotificationResponse> data,
      Long nextCursor,
      boolean hasNext,
      int size,
      long unreadCount) {
    return new NotificationListResponse(data, nextCursor, hasNext, size, (int) unreadCount);
  }
}
