package com.mymealserver.api.reaction.service;

import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.reaction.ReactionWriter;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.api.meal.service.MealLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionService {

    private final ReactionReader reactionReader;
    private final ReactionWriter reactionWriter;
    private final MealReader mealReader;
    private final MealLogService mealLogService;

    /**
     * Create a new reaction for a meal
     * Verifies meal ownership and validates data
     */
    @Transactional
    public ReactionResponse createReaction(Long memberId, Long mealId, ReactionRequest request) {
        log.debug("Creating reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // Check if reaction already exists
        Reaction existingReaction = reactionReader.findByMealId(mealId);
        if (existingReaction != null) {
            log.warn("Reaction already exists for mealId: {}", mealId);
            throw new BusinessException(ErrorCode.REACTION_ALREADY_EXISTS);
        }

        // Build reaction entity from request
        Reaction reaction = request.toEntity(mealId);

        // Calculate overall score and grade
        reaction.calculateOverallScore();

        // Save reaction
        Reaction savedReaction = reactionWriter.save(reaction);
        log.info("Created reaction for mealId: {} with overallScore: {}, grade: {}",
                mealId, savedReaction.getOverallScore(), savedReaction.getGrade());

        // Meal + MealAnalysis + Reaction → MealLog 생성 및 임베딩 (비동기)
        mealLogService.createMealLogAndEmbedAsync(mealId, savedReaction.getId());

        return ReactionResponse.from(savedReaction);
    }

    /**
     * Get reaction by meal ID
     * Returns null if no reaction exists
     */
    public ReactionResponse getReactionByMealId(Long memberId, Long mealId) {
        log.debug("Getting reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            log.warn("Member {} attempted to access meal {} owned by {}",
                    memberId, mealId, meal.getMemberId());
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // Find reaction
        Reaction reaction = reactionReader.findByMealId(mealId);
        if (reaction == null) {
            log.debug("No reaction found for mealId: {}", mealId);
            return null;
        }

        return ReactionResponse.from(reaction);
    }

    /**
     * 기존에 분석중인 서비스가 있으면 수정불가
     * 기존데이터를 다른 사진으로 대체
     */
    @Transactional
    public ReactionResponse updateReaction(Long memberId, Long mealId, ReactionRequest request) {
        log.debug("Updating reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findById(mealId);

        if (!meal.getMemberId().equals(memberId)) {
            log.warn("Member {} attempted to access meal {} owned by {}",
                    memberId, mealId, meal.getMemberId());
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // Find existing reaction
        Reaction reaction = reactionReader.findByMealId(mealId);
        if (reaction == null) {
            log.warn("No reaction found for mealId: {}", mealId);
            throw new BusinessException(ErrorCode.REACTION_NOT_FOUND);
        }

        // Update reaction fields
        reaction.update(request);

        // Recalculate overall score and grade
        reaction.calculateOverallScore();

        // Save updated reaction
        Reaction savedReaction = reactionWriter.save(reaction);
        log.info("Updated reaction for mealId: {} with overallScore: {}, grade: {}",
                mealId, savedReaction.getOverallScore(), savedReaction.getGrade());

        return ReactionResponse.from(savedReaction);
    }
}
