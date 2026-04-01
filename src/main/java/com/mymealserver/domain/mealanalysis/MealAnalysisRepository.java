package com.mymealserver.domain.mealanalysis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
  Optional<MealAnalysis> findByMealId(Long mealId);

  List<MealAnalysis> findAllByMealIdIn(List<Long> mealIds);
}
