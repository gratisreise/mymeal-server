package com.mymealserver.meal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealAnalysisService {

    // TODO: AI 식사 분석 로직 구현

    /**
     * 식사 AI 분석 비동기 처리
     * TODO: 실제 AI 분석 로직 구현 필요
     */
    @Async
    public void analyzeMealAsync(Long mealId, MultipartFile photo) {
        log.info("Starting AI analysis for mealId: {}", mealId);
        // TODO: AI 분석 구현 (Google Gemini Vision API 연동)
        // 1. S3에서 이미지 다운로드
        // 2. Gemini Vision API 호출
        // 3. 분석 결과 MealAnalysis 테이블에 저장
        // 4. Meal의 analysisStatus를 COMPLETED로 변경
        log.warn("AI analysis not implemented yet for mealId: {}", mealId);
    }
}
