package com.mymealserver.domain.notification;

import com.mymealserver.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWriter {

    private final NotificationRepository notificationRepository;

}
