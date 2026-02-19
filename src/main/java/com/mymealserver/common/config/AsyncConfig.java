package com.mymealserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리를 위한 설정
 * AI 식사 분석을 백그라운드 스레드에서 실행
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 식사 분석 전용 스레드 풀
     * AI 분석 작업을 독립된 스레드 풀에서 실행하여
     * 메인 요청 처리 스레드가 차단되지 않도록 함
     */
    @Bean(name = "mealAnalysisExecutor")
    public Executor mealAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("meal-analysis-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

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

    /**
     * MealLog 생성 및 임베딩 전용 스레드 풀
     * Meal + Reaction → MealLog 생성 및 벡터화 작업 처리
     */
    @Bean(name = "mealLogExecutor")
    public Executor mealLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("meallog-embedding-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
