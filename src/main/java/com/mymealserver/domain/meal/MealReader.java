package com.mymealserver.domain.meal;

import com.mymealserver.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealReader {

    private final MealRepository mealRepository;

}
