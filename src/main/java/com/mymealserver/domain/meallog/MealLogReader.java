package com.mymealserver.domain.meallog;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealLogReader {

  private final MealLogRepository mealLogRepository;

  public MealLog findById(Long id) {
    return mealLogRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_LOG_NOT_FOUND));
  }

  public MealLog findByMealId(Long mealId) {
    MealLog mealLog = mealLogRepository.findByMealId(mealId);
    if (mealLog == null) {
      throw new BusinessException(ErrorCode.MEAL_LOG_NOT_FOUND);
    }
    return mealLog;
  }

  public List<MealLog> findByMemberIdOrderByCreatedAtDesc(Long memberId) {
    return mealLogRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
  }

  public long countByMemberIdWithEmbedding(Long memberId) {
    return mealLogRepository.countByMemberIdAndEmbeddingIsNotNull(memberId);
  }

  public List<MealLog> findSimilarMealsByVector(Long memberId, String queryVector, int limit) {
    return mealLogRepository.findSimilarMealsByVector(memberId, queryVector, limit);
  }
}
