package com.mymealserver.domain.meal;

import com.mymealserver.repository.MealAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealAnalysisWriter {

    private final MealAnalysisRepository mealAnalysisRepository;

}
