package com.mymealserver.domain.mealanalysis;

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
}
