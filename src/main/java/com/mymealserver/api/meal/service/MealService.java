package com.mymealserver.meal.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.AnalysisStatus;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.meal.dto.request.MealCreateRequest;
import com.mymealserver.meal.dto.request.MealRetakePhotoRequest;
import com.mymealserver.meal.dto.response.AIAnalysisResponse;
import com.mymealserver.meal.dto.response.MealDetailResponse;
import com.mymealserver.meal.dto.response.MealResponse;
import com.mymealserver.reaction.domain.ReactionReader;
import com.mymealserver.reaction.dto.response.ReactionResponse;
import com.mymealserver.service.storage.FileStorageService;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.meal.MealAnalysisReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {

    private final MealReader mealReader;
    private final MealWriter mealWriter;
    private final ReactionReader reactionReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final FileStorageService fileStorageService;
    private final MealAnalysisService mealAnalysisService;

    /**
     * 식사 생성
     * 사진 업로드 -> Meal 저장 -> AI 분석 비동기 처리
     */
    @Transactional
    public MealResponse createMeal(Long memberId, MealCreateRequest request) {
        log.info("Creating meal for member: {}, mealType: {}", memberId, request.mealType());

        // 1. 사진 업로드
        String photoUrl = fileStorageService.uploadMealPhoto(request.photo(), memberId);
        String photoKey = fileStorageService.extractPhotoKey(photoUrl);

        // 2. Meal 엔티티 생성 및 저장
        LocalDateTime mealTime = request.mealTime() != null ? request.mealTime() : LocalDateTime.now();
        Meal meal = Meal.builder()
                .memberId(memberId)
                .mealType(request.mealType())
                .mealTime(mealTime)
                .photoUrl(photoUrl)
                .photoKey(photoKey)
                .memo(request.memo())
                .build();

        meal = mealWriter.save(meal);
        log.info("Meal created with id: {}", meal.getId());

        // 3. AI 분석 비동기 처리 (TODO: MealAnalysisService 구현 필요)
        mealAnalysisService.analyzeMealAsync(meal.getId(), request.photo());

        return MealResponse.from(meal, false);
    }

    /**
     * 식사 목록 조회
     * 페이지네이션 + 날짜 필터 + 식사 유형 필터
     */
    public Page<MealResponse> getMeals(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            MealType mealType,
            Pageable pageable
    ) {
        log.info("Fetching meals for member: {}, startDate: {}, endDate: {}, mealType: {}",
                memberId, startDate, endDate, mealType);

        Page<Meal> meals = mealReader.findByMemberId(memberId, startDate, endDate, mealType, pageable);

        return meals.map(meal -> {
            boolean hasReaction = reactionReader.existsByMealId(meal.getId());
            return MealResponse.from(meal, hasReaction);
        });
    }

    /**
     * 식사 상세 조회
     * AI 분석 결과 + 식후 반응 포함
     */
    public MealDetailResponse getMealDetail(Long memberId, Long mealId) {
        log.info("Fetching meal detail for member: {}, mealId: {}", memberId, mealId);

        Meal meal = mealReader.findById(mealId);

        // 권한 확인
        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // AI 분석 결과 조회
        AIAnalysisResponse aiAnalysis = null;
        if (meal.isAnalysisCompleted()) {
            aiAnalysis = mealAnalysisReader.findByMealId(mealId)
                    .map(AIAnalysisResponse::from)
                    .orElse(null);
        }

        // 식후 반응 조회
        ReactionResponse reaction = reactionReader.findByMealId(mealId)
                .map(ReactionResponse::from)
                .orElse(null);

        boolean hasReaction = reaction != null;

        return MealDetailResponse.from(
                meal,
                aiAnalysis,
                reaction,
                hasReaction
        );
    }

    /**
     * 식사 삭제 (Soft Delete)
     * 관련된 식후 반응 데이터는 보존
     */
    @Transactional
    public void deleteMeal(Long memberId, Long mealId) {
        log.info("Deleting meal for member: {}, mealId: {}", memberId, mealId);

        Meal meal = mealReader.findById(mealId);

        // 권한 확인
        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // Soft delete
        mealWriter.delete(meal);
        log.info("Meal soft deleted: {}", mealId);
    }

    /**
     * 사진 재촬영
     * 기존 식사의 사진만 교체하고 AI 재분석 진행
     * 기존 식후 반응 데이터는 보존
     */
    @Transactional
    public MealResponse retakePhoto(Long memberId, Long mealId, MealRetakePhotoRequest request) {
        log.info("Retaking photo for member: {}, mealId: {}", memberId, mealId);

        Meal meal = mealReader.findById(mealId);

        // 권한 확인
        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // 1. 새 사진 업로드
        String newPhotoUrl = fileStorageService.uploadMealPhoto(request.photo(), memberId);
        String newPhotoKey = fileStorageService.extractPhotoKey(newPhotoUrl);

        // 2. 기존 사진 삭제
        fileStorageService.deletePhoto(meal.getPhotoKey());

        // 3. Meal 엔티티 업데이트
        meal.updatePhoto(newPhotoUrl, newPhotoKey);
        meal.updateAnalysisStatus(AnalysisStatus.PENDING);
        mealWriter.save(meal);

        // 4. AI 재분석 비동기 처리
        mealAnalysisService.analyzeMealAsync(meal.getId(), request.photo());

        boolean hasReaction = reactionReader.existsByMealId(meal.getId());

        log.info("Photo retaken for meal: {}", mealId);

        return MealResponse.from(meal, hasReaction);
    }
}
