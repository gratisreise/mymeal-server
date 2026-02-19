package com.mymealserver.domain.meal;

import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.repository.MealAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealAnalysisWriter {

    private final MealAnalysisRepository mealAnalysisRepository;

    /**
     * MealAnalysis 저장
     */
    public MealAnalysis save(MealAnalysis mealAnalysis) {
        return mealAnalysisRepository.save(mealAnalysis);
    }
}
