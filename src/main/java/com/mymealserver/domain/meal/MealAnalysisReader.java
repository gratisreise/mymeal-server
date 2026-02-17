package com.mymealserver.domain.meal;

import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.repository.MealAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealAnalysisReader {

    private final MealAnalysisRepository mealAnalysisRepository;

    public Optional<MealAnalysis> findByMealId(Long mealId) {
        return mealAnalysisRepository.findByMealId(mealId);
    }
}
