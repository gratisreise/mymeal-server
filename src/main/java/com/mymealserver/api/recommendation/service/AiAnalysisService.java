package com.mymealserver.api.recommendation.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.enums.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Temporarily commented out for compilation
// import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    // Temporarily commented out for compilation
     private final ChatClient chatClient;

    // 음식 이미지 분석
    public FoodAnalysisResult analyzeFoodImage(Resource imageResource, MealType mealType) {
        try {
            log.info("Starting AI food analysis for mealType: {}", mealType);

            // 1. Gemini 프롬프트 생성 (한국어)
            String prompt = buildPrompt(mealType);

            // 2. Spring AI ChatClient 호출 (Resource + MIME 타입 직접 전달)
            FoodAnalysisResult result = chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text(prompt)
                            .media(MimeTypeUtils.IMAGE_JPEG, imageResource))  // MIME 타입 명시
                    .call()
                    .entity(FoodAnalysisResult.class);

            log.info("AI analysis completed: mealName={}, confidence={}",
                    result.mealName(), result.confidence());

            return result;

        } catch (Exception e) {
            log.error("AI analysis failed for mealType: {}", mealType, e);
            throw new BusinessException(ErrorCode.AI_ANALYSIS_ERROR);
        }
    }

    private String buildPrompt(MealType mealType) {
        return String.format("""
                Analyze this Korean meal photo and provide nutritional information.

                Context: This is a %s meal.

                Respond with JSON in this exact format:
                {
                  "mealName": "food name in Korean",
                  "calories": 0.0,
                  "carbohydrates": 0.0,
                  "protein": 0.0,
                  "fat": 0.0,
                  "confidence": 0.0
                }

                Guidelines:
                - mealName: Specific Korean dish name (e.g., "김치찌개", "불고기", "비빔밥")
                - Calories: For the entire meal shown
                - Confidence: 0.0 to 1.0 based on clarity of the image
                - If multiple dishes, focus on the main dish
                """,
                mealType.getDescription());
    }
}
