package com.mymealserver.domain.meal;

import com.mymealserver.repository.MealAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealAnalysisReader {

    private final MealAnalysisRepository mealAnalysisRepository;

}
