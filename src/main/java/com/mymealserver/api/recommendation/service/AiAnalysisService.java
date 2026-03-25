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

    public FoodAnalysisResult analyzeFoodImage(Resource imageResource, MealType mealType) {
        try {
            log.info("음식 이미지 분석 시작 - 식사 유형: {}", mealType.getDescription());

            // Gemini 프롬프트 생성 (한국어)
            String prompt = buildPrompt(mealType);

            // Spring AI ChatClient 호출 (Resource + MIME 타입 직접 전달)
            FoodAnalysisResult result = chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text(prompt)
                            .media(MimeTypeUtils.IMAGE_JPEG, imageResource))
                    .call()
                    .entity(FoodAnalysisResult.class);

            log.info("음식 이미지 분석 완료 - 음식명: {}", result.mealName());
            return result;
        } catch (Exception e) {
            log.error("음식 이미지 분석 실패 - 식사 유형: {}, 오류: {}", mealType.getDescription(), e.getMessage());
            throw BusinessException.error(ErrorCode.AI_ANALYSIS_ERROR);
        }
    }

    public RecommendationResult generateRecommendations(String ragPrompt) {
        try {
            log.info("식단 추천 생성 시작");

            RecommendationResult result = chatClient.prompt()
                    .user(ragPrompt)
                    .call()
                    .entity(RecommendationResult.class);

            log.info("식단 추천 생성 완료");
            return result;
        } catch (Exception e) {
            log.error("식단 추천 생성 실패 - 오류: {}", e.getMessage());
            throw BusinessException.error(ErrorCode.AI_ANALYSIS_ERROR);
        }
    }

    private String buildPrompt(MealType mealType) {
        return String.format("""
            당신은 한국 음식 영양 분석 전문가입니다.
            주어진 음식 사진을 보고 아래 지침에 따라 영양 정보를 분석하세요.

            [식사 유형]
            %s

            [분석 지침]
            - 사진에 음식이 여러 개 보이는 경우, 가장 주된 음식(메인 요리) 기준으로 분석하세요.
            - 반찬류(김치, 나물 등 소량 곁들임 음식)는 분석 대상에서 제외하세요.
            - 음식의 양은 사진에 보이는 1인분 기준으로 추정하세요.
            - 음식을 식별할 수 없는 경우, mealName을 "알 수 없음"으로 설정하고 나머지 값은 0으로 입력하세요.

            [응답 형식]
            반드시 아래 JSON 형식만 출력하세요. 설명, 마크다운, 코드블록은 절대 포함하지 마세요.

            {
              "mealName": "한국어 음식명 (예: 김치찌개, 불고기, 비빔밥)",
              "calories": 0.0,
              "carbohydrates": 0.0,
              "protein": 0.0,
              "fat": 0.0
            }

            [단위 기준]
            - calories: kcal
            - carbohydrates, protein, fat: g (그램)
            """,
            mealType.getDescription());
    }
}
