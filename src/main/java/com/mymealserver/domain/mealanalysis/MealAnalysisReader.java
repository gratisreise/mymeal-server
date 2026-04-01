package com.mymealserver.domain.mealanalysis;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealAnalysisReader {

  private final MealAnalysisRepository mealAnalysisRepository;

  public Optional<MealAnalysis> findByMealId(Long mealId) {
    return mealAnalysisRepository.findByMealId(mealId);
  }

  public List<MealAnalysis> findAllByMealIds(List<Long> mealIds) {
    if (mealIds.isEmpty()) {
      return List.of();
    }
    return mealAnalysisRepository.findAllByMealIdIn(mealIds);
  }
}
