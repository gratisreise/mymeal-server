package com.mymealserver.external.batch.scheduler;

import com.mymealserver.external.batch.job.RecommendationGenerationJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final Job recommendationGenerationJob;
    private final JobLauncher jobLauncher;

    /**
     * Trigger recommendation generation batch job daily at 1 AM
     * Cron expression: seconds, minutes, hours, day-of-month, month, day-of-week
     * 0 0 1 * * ? = At 01:00:00 AM every day
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyRecommendations() {
        log.info("=== Starting daily recommendation generation at {} ===", LocalDateTime.now());

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDate("runDate", LocalDate.now())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(recommendationGenerationJob, jobParameters);

            log.info("=== Daily recommendation generation completed successfully ===");

        } catch (Exception e) {
            log.error("Failed to execute recommendation generation job", e);
        }
    }

    /**
     * Optional: Manual trigger for testing purposes
     * Can be called via API endpoint for testing
     */
    public void triggerRecommendationGeneration() throws Exception {
        log.info("=== Manually triggering recommendation generation at {} ===", LocalDateTime.now());

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDate("runDate", LocalDate.now())
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("trigger", "manual")
                    .toJobParameters();

            jobLauncher.run(recommendationGenerationJob, jobParameters);

            log.info("=== Manual recommendation generation completed successfully ===");

        } catch (Exception e) {
            log.error("Failed to execute manual recommendation generation job", e);
            throw e;
        }
    }
}
