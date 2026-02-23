package com.mymealserver.domain.mealanalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
    Optional<MealAnalysis> findByMealId(Long mealId);
}
