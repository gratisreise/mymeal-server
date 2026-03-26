package com.mymealserver.external.batch;

import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.recommendation.Recommendation;
import java.util.List;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationGenerationJobConfig {

  private static final String JOB_NAME = "recommendationGenerationJob";
  private static final String STEP_NAME = "recommendationGenerationStep";

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean(name = JOB_NAME)
  public Job recommendationGenerationJob(@Qualifier(STEP_NAME) Step recommendationGenerationStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(recommendationGenerationStep)
        .build();
  }

  @Bean(name = STEP_NAME)
  @StepScope
  public Step recommendationGenerationStep(
      MemberItemReader reader, RecommendationProcessor processor, RecommendationItemWriter writer) {
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
        .build();
  }
}
