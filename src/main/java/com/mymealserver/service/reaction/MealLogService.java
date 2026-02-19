package com.mymealserver.service.reaction;

import com.mymealserver.domain.meal.MealAnalysisReader;
import com.mymealserver.domain.meal.MealLogReader;
import com.mymealserver.domain.meal.MealLogWriter;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.entity.MealLog;
import com.mymealserver.entity.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealLogService {

    private final MealReader mealReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final ReactionReader reactionReader;
    private final MealLogWriter mealLogWriter;
    private final EmbeddingModel embeddingModel;

    /**
     * Meal + MealAnalysis + Reaction → MealLog 생성 및 임베딩 (비동기)
     *
     * @param mealId     식사 ID
     * @param reactionId 반응 ID
     */
    @Async("mealLogExecutor")
    public void createMealLogAndEmbedAsync(Long mealId, Long reactionId) {
        try {
            // 1. Meal, MealAnalysis, Reaction 조회
            Meal meal = mealReader.findById(mealId);

            MealAnalysis analysis = mealAnalysisReader.findByMealId(mealId)
                    .orElseThrow(() -> new IllegalArgumentException("MealAnalysis not found for mealId: " + mealId));

            Reaction reaction = reactionReader.findByMealId(mealId);
            if (reaction == null) {
                throw new IllegalArgumentException("Reaction not found for mealId: " + mealId);
            }

            // 2. MealLog 생성
            MealLog mealLog = MealLog.from(meal, analysis, reaction);
            log.debug("Created MealLog for meal: {}, combinedSummary: {}",
                    mealId, mealLog.getCombinedSummary());

            // 3. combinedSummary 임베딩 생성
            try {
                EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(
                        Arrays.asList(mealLog.getCombinedSummary())
                );

                float[] embeddingArray = embeddingResponse.getResults().get(0).getOutput();

                // float[] → Float[] 변환
                Float[] embeddingBoxed = new Float[embeddingArray.length];
                for (int i = 0; i < embeddingArray.length; i++) {
                    embeddingBoxed[i] = embeddingArray[i];
                }

                mealLog.setEmbedding(embeddingBoxed);
                log.debug("Generated embedding for meal: {}, dimension: {}",
                        mealId, embeddingBoxed.length);

            } catch (Exception e) {
                log.error("Failed to generate embedding for meal: {}", mealId, e);
                // 임베딩 실패 시에도 MealLog는 저장 (embedding은 null)
            }

            // 4. MealLog 저장
            mealLogWriter.save(mealLog);
            log.info("MealLog created and saved for meal: {}", mealId);

        } catch (Exception e) {
            log.error("Failed to create MealLog for meal: {}, reaction: {}", mealId, reactionId, e);
        }
    }
}
