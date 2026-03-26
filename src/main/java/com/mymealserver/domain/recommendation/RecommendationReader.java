package com.mymealserver.domain.recommendation;

import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationReader {

  private final RecommendationRepository recommendationRepository;

  public Recommendation findById(Long id) {
    return recommendationRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
  }

  public List<Recommendation> findByMemberId(Long memberId) {
    return recommendationRepository.findByMemberIdAndDeletedAtIsNullOrderByScheduledTimeAsc(
        memberId);
  }

  public List<Recommendation> findByMemberIdAndMealType(Long memberId, MealType mealType) {
    return recommendationRepository
        .findByMemberIdAndMealTypeAndDeletedAtIsNullOrderByScheduledTimeDesc(memberId, mealType);
  }

  public List<Recommendation> findByMemberIdAndDateRange(Long memberId, LocalDate date) {
    LocalDateTime startDateTime = date.atStartOfDay();
    LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();

    return recommendationRepository.findByMemberIdAndDateRange(
        memberId, startDateTime, endDateTime);
  }

  public List<Recommendation> findPendingRecommendations(LocalDateTime scheduledTime) {
    return recommendationRepository.findPendingRecommendations(scheduledTime);
  }

  public boolean existsTodayRecommendation(Long memberId, MealType mealType) {
    return recommendationRepository.existsTodayRecommendation(memberId, mealType);
  }

  public boolean existsRecommendationInRange(
      Long memberId, MealType mealType, LocalDateTime startTime, LocalDateTime endTime) {
    return recommendationRepository
        .existsByMemberIdAndMealTypeAndScheduledTimeBetweenAndDeletedAtIsNull(
            memberId, mealType, startTime, endTime);
  }
}
