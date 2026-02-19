package com.mymealserver.api.meal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.Food;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.entity.enums.AnalysisStatus;
import com.mymealserver.domain.food.FoodReader;
import com.mymealserver.domain.food.FoodWriter;
import com.mymealserver.domain.meal.MealAnalysisWriter;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.api.recommendation.service.FoodAnalysisResult;
import com.mymealserver.entity.enums.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 식사 AI 분석 서비스
 * Meal 생성 후 백그라운드에서 AI 분석을 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MealAnalysisService {

    private final AiAnalysisService aiAnalysisService;
    private final MealReader mealReader;
    private final MealWriter mealWriter;
    private final MealAnalysisWriter mealAnalysisWriter;
    private final FoodReader foodReader;
    private final FoodWriter foodWriter;
    private final ObjectMapper objectMapper;

    /**
     * 식사 AI 분석 비동기 처리
     * Meal 생성 후 즉시 호출되어 백그라운드에서 실행
     *
     * @param mealId        식사 ID
     * @param mealType      식사 유형
     * @param imageResource MultipartFile에서 변환된 ByteArrayResource (스레드 안전)
     */
    @Async("mealAnalysisExecutor")
    @Transactional
    public void analyzeMealAsync(Long mealId, MealType mealType, Resource imageResource) {
        log.info("Starting AI analysis for mealId: {}", mealId);

        try {
            // 1. Meal 조회
            Meal meal = mealReader.findById(mealId);

            // 2. AI 분석 요청 (Resource 그대로 사용, S3 다운로드 불필요)
            FoodAnalysisResult analysis = aiAnalysisService.analyzeFoodImage(
                    imageResource,
                    mealType
            );

            // 3. Food 매칭 (이름이 같으면 foodId 연결)
            Food food = foodReader.findByName(analysis.mealName())
                    .orElseGet(() -> createNewFood(analysis));

            // 4. MealAnalysis 저장
            MealAnalysis mealAnalysis = MealAnalysis.builder()
                    .mealId(mealId)
                    .foodId(food.getId())
                    .mealName(analysis.mealName())
                    .calories(analysis.calories())
                    .carbohydrates(analysis.carbohydrates())
                    .protein(analysis.protein())
                    .fat(analysis.fat())
                    .confidence(analysis.confidence())
                    .rawResponse(objectMapper.writeValueAsString(analysis))
                    .build();

            mealAnalysisWriter.save(mealAnalysis);

            // 5. Meal 상태를 COMPLETED로 업데이트
            meal.updateAnalysisStatus(AnalysisStatus.COMPLETED);
            mealWriter.save(meal);

            log.info("AI analysis completed for mealId: {}, mealName: {}",
                    mealId, analysis.mealName());

        } catch (Exception e) {
            log.error("AI analysis failed for mealId: {}", mealId, e);
            handleAnalysisFailure(mealId, e);
        }
    }

    /**
     * 새로운 Food 생성
     * AI가 식별한 음식이 Food 테이블에 없으면 자동 생성
     */
    private Food createNewFood(FoodAnalysisResult analysis) {
        Food food = Food.builder()
                .name(analysis.mealName())
                .calories(analysis.calories())
                .carbohydrates(analysis.carbohydrates())
                .protein(analysis.protein())
                .fat(analysis.fat())
                .averageScore(0.0)
                .mealCount(0)
                .build();

        return foodWriter.save(food);
    }

    /**
     * 분석 실패 처리
     * AI 분석 실패 시 Meal 상태를 FAILED로 변경하고 실패 기록 저장
     */
    private void handleAnalysisFailure(Long mealId, Exception e) {
        try {
            Meal meal = mealReader.findById(mealId);
            meal.updateAnalysisStatus(AnalysisStatus.FAILED);
            mealWriter.save(meal);

            // 실패 기록 저장
            MealAnalysis failureAnalysis = MealAnalysis.builder()
                    .mealId(mealId)
                    .mealName("분석 실패")
                    .confidence(0.0)
                    .rawResponse("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();

            mealAnalysisWriter.save(failureAnalysis);

        } catch (Exception ex) {
            log.error("Failed to handle analysis error for mealId: {}", mealId, ex);
        }
    }
}
