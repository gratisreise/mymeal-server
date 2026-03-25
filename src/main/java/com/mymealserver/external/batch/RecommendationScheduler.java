package com.mymealserver.external.batch;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final Job recommendationGenerationJob;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 0 16 * * ?")
    public void generateDailyRecommendations() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("runDate", LocalDate.now())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(recommendationGenerationJob, jobParameters);
    }

    public void triggerRecommendationGeneration() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("runDate", LocalDate.now())
                .addLong("timestamp", System.currentTimeMillis())
                .addString("trigger", "manual")
                .toJobParameters();

        jobLauncher.run(recommendationGenerationJob, jobParameters);
    }
}
