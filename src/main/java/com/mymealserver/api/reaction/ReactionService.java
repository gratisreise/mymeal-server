package com.mymealserver.api.reaction.service;

import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.reaction.ReactionWriter;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionService {

    private final ReactionReader reactionReader;
    private final ReactionWriter reactionWriter;
    private final MealReader mealReader;

    /**
     * Create a new reaction for a meal
     * Verifies meal ownership and validates data
     */
    @Transactional
    public ReactionResponse createReaction(Long memberId, Long mealId, ReactionRequest request) {
        log.debug("Creating reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findByIdOptional(mealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));

        if (!meal.getMemberId().equals(memberId)) {
            log.warn("Member {} attempted to access meal {} owned by {}",
                    memberId, mealId, meal.getMemberId());
            throw new BusinessException(ErrorCode.MEAL_FORBIDDEN);
        }

        // Check if reaction already exists
        Reaction existingReaction = reactionReader.findByMealId(mealId);
        if (existingReaction != null) {
            log.warn("Reaction already exists for mealId: {}", mealId);
            throw new BusinessException(ErrorCode.REACTION_ALREADY_EXISTS);
        }

        // Build reaction entity from request
        Reaction reaction = Reaction.builder()
                .mealId(mealId)
                .digestionLevel(request.digestionLevel().shortValue())
                .fullnessLevel(request.fullnessLevel().shortValue())
                .energyLevel(request.energyLevel().shortValue())
                .hasHeartburn(Optional.ofNullable(request.hasHeartburn()).orElse(false))
                .hasGas(Optional.ofNullable(request.hasGas()).orElse(false))
                .hasBloating(Optional.ofNullable(request.hasBloating()).orElse(false))
                .hasHeadache(Optional.ofNullable(request.hasHeadache()).orElse(false))
                .memo(request.memo())
                .build();

        // Calculate overall score and grade
        reaction.calculateOverallScore();

        // Save reaction
        Reaction savedReaction = reactionWriter.save(reaction);
        log.info("Created reaction for mealId: {} with overallScore: {}, grade: {}",
                mealId, savedReaction.getOverallScore(), savedReaction.getGrade());

        return ReactionResponse.from(savedReaction);
    }

    /**
     * Get reaction by meal ID
     * Returns null if no reaction exists
     */
    public ReactionResponse getReactionByMealId(Long memberId, Long mealId) {
        log.debug("Getting reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findByIdOptional(mealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));

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
     * Update existing reaction for a meal
     * Verifies meal ownership and reaction exists
     */
    @Transactional
    public ReactionResponse updateReaction(Long memberId, Long mealId, ReactionRequest request) {
        log.debug("Updating reaction for memberId: {}, mealId: {}", memberId, mealId);

        // Verify meal exists and belongs to member
        Meal meal = mealReader.findByIdOptional(mealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));

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

        // Update reaction fields directly
        reaction.setDigestionLevel(request.digestionLevel().shortValue());
        reaction.setFullnessLevel(request.fullnessLevel().shortValue());
        reaction.setEnergyLevel(request.energyLevel().shortValue());
        reaction.setHasHeartburn(Optional.ofNullable(request.hasHeartburn()).orElse(false));
        reaction.setHasGas(Optional.ofNullable(request.hasGas()).orElse(false));
        reaction.setHasBloating(Optional.ofNullable(request.hasBloating()).orElse(false));
        reaction.setHasHeadache(Optional.ofNullable(request.hasHeadache()).orElse(false));
        reaction.setMemo(request.memo());

        // Recalculate overall score and grade
        reaction.calculateOverallScore();

        // Save updated reaction
        Reaction savedReaction = reactionWriter.save(reaction);
        log.info("Updated reaction for mealId: {} with overallScore: {}, grade: {}",
                mealId, savedReaction.getOverallScore(), savedReaction.getGrade());

        return ReactionResponse.from(savedReaction);
    }
}
