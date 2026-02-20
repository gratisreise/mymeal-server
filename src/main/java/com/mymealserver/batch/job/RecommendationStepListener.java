package com.mymealserver.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecommendationStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("=== Starting Recommendation Generation Step ===");
        log.info("Job name: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
        log.info("Step name: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("=== Recommendation Generation Step Completed ===");
        log.info("Read count: {}", stepExecution.getReadCount());
        log.info("Write count: {}", stepExecution.getWriteCount());
        log.info("Commit count: {}", stepExecution.getCommitCount());
        log.info("Skip count: {}", stepExecution.getSkipCount());
        log.info("Rollback count: {}", stepExecution.getRollbackCount());
        log.info("Exit status: {}", stepExecution.getExitStatus());

        return stepExecution.getExitStatus();
    }
}
