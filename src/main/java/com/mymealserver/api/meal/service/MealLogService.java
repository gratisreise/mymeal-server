package com.mymealserver.api.meal.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.meallog.MealLog;
import com.mymealserver.domain.meallog.MealLogWriter;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealLogService {

    private final MealReader mealReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final ReactionReader reactionReader;
    private final MealLogWriter mealLogWriter;
    private final EmbeddingModel embeddingModel;


    @Async("mealLogExecutor")
    public void createMealLogAndEmbedAsync(Long mealId, Long reactionId) {
        try {
            // 1. Meal, MealAnalysis, Reaction 조회
            Meal meal = mealReader.findById(mealId);

            MealAnalysis analysis = mealAnalysisReader.findByMealId(mealId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_ANALYSIS_NOT_FOUND));

            Reaction reaction = reactionReader.findByMealId(mealId);
            if (reaction == null) {
                throw new BusinessException(ErrorCode.REACTION_NOT_FOUND);
            }

            // 2. MealLog 생성
            MealLog mealLog = MealLog.from(meal, analysis, reaction);

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

            } catch (Exception e) {
                log.error("식사 ID {}의 임베딩 생성에 실패했습니다", mealId, e);
                // 임베딩 실패 시에도 MealLog는 저장 (embedding은 null)
            }

            // 4. MealLog 저장
            mealLogWriter.save(mealLog);

        } catch (Exception e) {
            log.error("식사 ID {}, 반응 ID {}의 MealLog 생성에 실패했습니다", mealId, reactionId, e);
        }
    }
}
