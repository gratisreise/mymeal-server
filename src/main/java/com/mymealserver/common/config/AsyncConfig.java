package com.mymealserver.common.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "mealAnalysisExecutor")
  public Executor mealAnalysisExecutor() {
    return new VirtualThreadTaskExecutor("meal-analysis-");
  }

  @Bean(name = "mealLogExecutor")
  public Executor mealLogExecutor() {
    return new VirtualThreadTaskExecutor("meallog-embedding-");
  }

  @Bean(name = "fcmNotificationExecutor")
  public Executor fcmNotificationExecutor() {
    return new VirtualThreadTaskExecutor("fcm-notification-");
  }
}
