package com.mymealserver.api.reaction.service;

import com.mymealserver.api.meal.service.MealLogService;
import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.reaction.ReactionWriter;
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

  @Transactional
  public ReactionResponse createReaction(Long memberId, Long mealId, ReactionRequest request) {
    // 식사가 존재하고 회원의 소유인지 확인
    Meal meal = mealReader.findById(mealId);

    if (!meal.getMemberId().equals(memberId)) {
      throw BusinessException.error(ErrorCode.MEAL_FORBIDDEN);
    }

    // 반응이 이미 존재하는지 확인
    Reaction existingReaction = reactionReader.findByMealId(mealId);
    if (existingReaction != null) {
      log.warn("식사 ID {}에 대한 반응이 이미 존재합니다", mealId);
      throw BusinessException.error(ErrorCode.REACTION_ALREADY_EXISTS);
    }

    // 요청에서 반응 엔티티 생성
    Reaction reaction = request.toEntity(mealId);

    // 전체 점수 및 등급 계산
    reaction.calculateOverallScore();

    // 반응 저장
    Reaction savedReaction = reactionWriter.save(reaction);

    // Meal + MealAnalysis + Reaction → MealLog 생성 및 임베딩 (비동기)
    mealLogService.createMealLogAndEmbedAsync(mealId, savedReaction.getId());

    return ReactionResponse.from(savedReaction);
  }

  public ReactionResponse getReactionByMealId(Long memberId, Long mealId) {
    // 식사가 존재하고 회원의 소유인지 확인
    Meal meal = mealReader.findById(mealId);

    if (!meal.getMemberId().equals(memberId)) {
      log.warn("회원 {}이 소유하지 않은 식사 {}에 접근 시도", memberId, mealId);
      throw BusinessException.error(ErrorCode.MEAL_FORBIDDEN);
    }

    // 반응 조회
    Reaction reaction = reactionReader.findByMealId(mealId);
    if (reaction == null) {
      return null;
    }

    return ReactionResponse.from(reaction);
  }

  @Transactional
  public ReactionResponse updateReaction(Long memberId, Long mealId, ReactionRequest request) {
    // 식사가 존재하고 회원의 소유인지 확인
    Meal meal = mealReader.findById(mealId);

    if (!meal.getMemberId().equals(memberId)) {
      log.warn("회원 {}이 소유하지 않은 식사 {}에 접근 시도", memberId, mealId);
      throw BusinessException.error(ErrorCode.MEAL_FORBIDDEN);
    }

    // 기존 반응 조회
    Reaction reaction = reactionReader.findByMealId(mealId);
    if (reaction == null) {
      log.warn("식사 ID {}에 대한 반응을 찾을 수 없습니다", mealId);
      throw BusinessException.error(ErrorCode.REACTION_NOT_FOUND);
    }

    // 반응 필드 업데이트
    reaction.update(request);

    // 전체 점수 및 등급 재계산
    reaction.calculateOverallScore();

    // 업데이트된 반응 저장
    Reaction savedReaction = reactionWriter.save(reaction);

    return ReactionResponse.from(savedReaction);
  }
}
