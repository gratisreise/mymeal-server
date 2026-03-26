package com.mymealserver.api.notification.dto.response;

import com.mymealserver.common.response.PageResponse.Pagination;
import java.util.List;

public record NotificationListResponse(
    List<NotificationResponse> data, Pagination pagination, Integer unreadCount) {
  public static NotificationListResponse of(
      List<NotificationResponse> data, Pagination pagination, long unreadCount) {
    return new NotificationListResponse(data, pagination, (int) unreadCount);
  }
}
