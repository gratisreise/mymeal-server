package com.mymealserver.repository;

import com.mymealserver.entity.MealAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
}
