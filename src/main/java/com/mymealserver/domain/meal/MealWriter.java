package com.mymealserver.domain.meal;

import com.mymealserver.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealWriter {

    private final MealRepository mealRepository;

}
