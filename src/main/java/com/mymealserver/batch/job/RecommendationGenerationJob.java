package com.mymealserver.batch.job;

import com.mymealserver.batch.processor.RecommendationProcessor;
import com.mymealserver.batch.reader.MemberItemReader;
import com.mymealserver.batch.writer.RecommendationItemWriter;
import com.mymealserver.entity.Member;
import com.mymealserver.entity.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationGenerationJob {

    private static final String JOB_NAME = "recommendationGenerationJob";
    private static final String STEP_NAME = "recommendationGenerationStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = JOB_NAME)
    public Job recommendationGenerationJob(
            @Qualifier(STEP_NAME) Step recommendationGenerationStep
    ) {
        log.info("Configuring recommendation generation job");

        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(recommendationGenerationStep)
                .build();
    }

    @Bean(name = STEP_NAME)
    @StepScope
    public Step recommendationGenerationStep(
            MemberItemReader reader,
            RecommendationProcessor processor,
            RecommendationItemWriter writer
    ) {
        log.info("Configuring recommendation generation step");

        return new StepBuilder(STEP_NAME, jobRepository)
                .<Member, List<Recommendation>>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .skip(Exception.class)
                .skipLimit(10)
                .listener(new RecommendationStepListener())
                .build();
    }
}
