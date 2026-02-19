package com.mymealserver.api.meal.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.AnalysisStatus;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.api.meal.dto.request.MealRetakePhotoRequest;
import com.mymealserver.api.meal.dto.response.AIAnalysisResponse;
import com.mymealserver.api.meal.dto.response.MealDetailResponse;
import com.mymealserver.api.meal.dto.response.MealResponse;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.service.storage.FileStorageService;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.meal.MealAnalysisReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Transactional
    public MealResponse createMeal(Long memberId, MultipartFile photo, MealType mealType) {
        Resource imageResource = photo.getResource();
        String photoUrl = fileStorageService.uploadMealPhoto(photo, memberId);
        String photoKey = fileStorageService.extractPhotoKey(photoUrl);

        Meal meal = Meal.builder()
                .memberId(memberId)
                .mealType(mealType)
                .photoUrl(photoUrl)
                .photoKey(photoKey)
                .mealTime(LocalDateTime.now())
                .build();

        meal = mealWriter.save(meal);
        mealAnalysisService.analyzeMealAsync(meal.getId(), mealType, imageResource);

        return MealResponse.from(meal, false);
    }

    public Page<MealResponse> getMeals(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            MealType mealType,
            Pageable pageable
    ) {
        Page<Meal> meals = mealReader.findByMemberId(memberId, startDate, endDate, mealType, pageable);

        return meals.map(meal -> {
            boolean hasReaction = reactionReader.existsByMealId(meal.getId());
            return MealResponse.from(meal, hasReaction);
        });
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
    public void deleteMeal(Long memberId, Long mealId) {
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        mealWriter.delete(meal);
    }

    @Transactional
    public MealResponse retakePhoto(Long memberId, Long mealId, MealRetakePhotoRequest request) {
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        MultipartFile photo = request.photo();
        Resource imageResource = photo.getResource();
        String newPhotoUrl = fileStorageService.uploadMealPhoto(photo, memberId);
        String newPhotoKey = fileStorageService.extractPhotoKey(newPhotoUrl);

        fileStorageService.deletePhoto(meal.getPhotoKey());

        meal.updatePhoto(newPhotoUrl, newPhotoKey);
        meal.updateAnalysisStatus(AnalysisStatus.PENDING);
        mealWriter.save(meal);

        mealAnalysisService.analyzeMealAsync(meal.getId(), meal.getMealType(), imageResource);

        boolean hasReaction = reactionReader.existsByMealId(meal.getId());

        return MealResponse.from(meal, hasReaction);
    }
}
