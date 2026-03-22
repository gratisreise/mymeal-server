package com.mymealserver.api.recommendation.service;

import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {


    private final ChatClient chatClient;


    // 음식 이미지 분석
    public FoodAnalysisResult analyzeFoodImage(Resource imageResource, MealType mealType) {
        try {

            // 1. Gemini 프롬프트 생성 (한국어)
            String prompt = buildPrompt(mealType);

            // 2. Spring AI ChatClient 호출 (Resource + MIME 타입 직접 전달)
            return chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text(prompt)
                            .media(MimeTypeUtils.IMAGE_JPEG, imageResource))  // MIME 타입 명시
                    .call()
                    .entity(FoodAnalysisResult.class);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_ERROR);
        }
    }

    /**
     * RAG 프롬프트 기반 식단 추천 생성
     * 배치 처리에서 호출됨
     */
    public RecommendationResult generateRecommendations(String ragPrompt) {
        try {
            return chatClient.prompt()
                    .user(ragPrompt)
                    .call()
                    .entity(RecommendationResult.class);
        } catch (Exception e) {
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
                }

                Guidelines:
                - mealName: Specific Korean dish name (e.g., "김치찌개", "불고기", "비빔밥")
                - Calories: For the entire meal shown
                - If multiple dishes, focus on the main dish
                """,
                mealType.getDescription());
    }
}
