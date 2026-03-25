package com.mymealserver.api.meal.service;

import com.mymealserver.api.meal.dto.response.AIAnalysisResponse;
import com.mymealserver.api.meal.dto.response.MealDetailResponse;
import com.mymealserver.api.meal.dto.response.MealResponse;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.external.redis.NotificationPayload;
import com.mymealserver.external.redis.UnifiedNotificationService;
import com.mymealserver.external.s3.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {

    private final MealReader mealReader;
    private final MealWriter mealWriter;
    private final ReactionReader reactionReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final S3Service s3Service;
    private final MealAnalysisService mealAnalysisService;
    private final UnifiedNotificationService unifiedNotificationService;

    @Transactional
    public MealResponse createMeal(Long memberId, MultipartFile photo, MealType mealType) {
        Resource imageResource = photo.getResource();
        String photoUrl = s3Service.uploadMealPhoto(photo, memberId);
        String photoKey = s3Service.extractPhotoKey(photoUrl);

        Meal meal = createNewMeal(memberId, mealType, photoUrl, photoKey);

        meal = mealWriter.save(meal);
        mealAnalysisService.analyzeMealAsync(meal.getId(), mealType, imageResource);

        // 알림 예약 (식후 60분 후 반응 알림)
        NotificationPayload payload = NotificationPayload.forReactionReminder(
                memberId,
                meal.getId()
        );
        unifiedNotificationService.schedule(payload, meal.getMealTime().plusMinutes(60));

        return MealResponse.from(meal, false);
    }

    public PageResponse<MealResponse> getMeals(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            MealType mealType,
            Pageable pageable
    ) {
        Page<MealResponse> meals = mealReader.findByMemberId(memberId, startDate, endDate, mealType, pageable)
            .map(meal -> {
            boolean hasReaction = reactionReader.existsByMealId(meal.getId());
            return MealResponse.from(meal, hasReaction);
        });;

        return PageResponse.from(meals);
    }

    public MealDetailResponse getMealDetail(Long memberId, Long mealId) {
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        AIAnalysisResponse aiAnalysis = null;
        if (meal.isAnalysisCompleted()) {
            aiAnalysis = mealAnalysisReader.findByMealId(mealId)
                    .map(AIAnalysisResponse::from)
                    .orElse(null);
        }

        Reaction reaction  = reactionReader.findByMealId(mealId);
        ReactionResponse reactionResponse = ReactionResponse.from(reaction);

        boolean hasReaction = reaction != null;

        return MealDetailResponse.from(
                meal,
                aiAnalysis,
                reactionResponse,
                hasReaction
        );
    }

    @Transactional
    public MealResponse retakePhoto(Long memberId, Long mealId, MultipartFile photo) {
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        Resource imageResource = photo.getResource();
        String newPhotoUrl = s3Service.uploadMealPhoto(photo, memberId);
        String newPhotoKey = s3Service.extractPhotoKey(newPhotoUrl);

        s3Service.deletePhoto(meal.getPhotoKey());

        meal.updatePhoto(newPhotoUrl, newPhotoKey);
        meal.updateAnalysisStatus(AnalysisStatus.PENDING);
        mealWriter.save(meal);

        mealAnalysisService.analyzeMealAsync(meal.getId(), meal.getMealType(), imageResource);

        boolean hasReaction = reactionReader.existsByMealId(meal.getId());

        return MealResponse.from(meal, hasReaction);
    }

    @Transactional
    public void deleteMeal(Long memberId, Long mealId) {
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        mealWriter.delete(meal);
    }

    private Meal createNewMeal(Long memberId, MealType mealType, String photoUrl,
        String photoKey) {
        return Meal.builder()
            .memberId(memberId)
            .mealType(mealType)
            .photoUrl(photoUrl)
            .photoKey(photoKey)
            .mealTime(LocalDateTime.now())
            .build();
    }
}
