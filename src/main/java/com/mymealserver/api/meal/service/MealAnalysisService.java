package com.mymealserver.api.meal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.api.recommendation.service.dto.FoodAnalysisResult;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.food.Food;
import com.mymealserver.domain.food.FoodReader;
import com.mymealserver.domain.food.FoodWriter;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  @Async("mealAnalysisExecutor")
  public void analyzeMealAsync(Long mealId, MealType mealType, Resource imageResource) {
    try {
      Meal meal = mealReader.findById(mealId);

      FoodAnalysisResult analysis = aiAnalysisService.analyzeFoodImage(imageResource, mealType);

      Food food =
          foodReader.findByName(analysis.mealName()).orElseGet(() -> createNewFood(analysis));

      MealAnalysis mealAnalysis = createMealAnalysis(mealId, food, analysis);

      mealAnalysisWriter.save(mealAnalysis);

      meal.updateAnalysisStatus(AnalysisStatus.COMPLETED);

    } catch (Exception e) {
      log.error("AI 분석 실패 - mealId: {}", mealId, e);
      handleAnalysisFailure(mealId, e);
    }
  }

  private MealAnalysis createMealAnalysis(Long mealId, Food food, FoodAnalysisResult analysis)
      throws JsonProcessingException {
    return MealAnalysis.builder()
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
  }

  private Food createNewFood(FoodAnalysisResult analysis) {
    Food food =
        Food.builder()
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

  private void handleAnalysisFailure(Long mealId, Exception e) {
    try {
      Meal meal = mealReader.findById(mealId);
      meal.updateAnalysisStatus(AnalysisStatus.FAILED);
      mealWriter.save(meal);

      MealAnalysis failureAnalysis =
          MealAnalysis.builder()
              .mealId(mealId)
              .mealName("분석 실패")
              .confidence(0.0)
              .rawResponse("{\"error\":\"" + e.getMessage() + "\"}")
              .build();

      mealAnalysisWriter.save(failureAnalysis);

    } catch (Exception ex) {
      log.error("분석 실패 처리 오류 - mealId: {}", mealId, ex);
    }
  }
}
