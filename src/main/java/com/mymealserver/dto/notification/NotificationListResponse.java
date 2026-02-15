package com.mymealserver.dto.notification;

import com.mymealserver.dto.common.PaginationResponse;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> content,
        PaginationResponse pagination,
        Integer unreadCount
) {
}
