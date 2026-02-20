package com.mymealserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Scheduler configuration for recommendation notification polling
    // Enables @Scheduled annotations in:
    // - NotificationPollingScheduler (1-minute polling)
    // - RecommendationScheduler (daily batch job scheduling)
}
