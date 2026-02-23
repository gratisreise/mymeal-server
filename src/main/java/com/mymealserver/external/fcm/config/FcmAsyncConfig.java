package com.mymealserver.external.fcm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * FCM 알림 발송을 위한 비동기 처리 설정
 */
@Configuration
@EnableAsync
public class FcmAsyncConfig {

    /**
     * FCM 알림 발송 전용 스레드 풀
     * Firebase FCM 발송을 비동기로 처리
     */
    @Bean(name = "fcmNotificationExecutor")
    public Executor fcmNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("fcm-notification-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
