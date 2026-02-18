package com.mymealserver.notification.dto.response;

import com.mymealserver.common.response.classes.Pagination;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> data,
        Pagination pagination,
        Integer unreadCount
) {
    public static NotificationListResponse of(
            List<NotificationResponse> data,
            Pagination pagination,
            long unreadCount
    ) {
        return new NotificationListResponse(data, pagination, (int) unreadCount);
    }
}
