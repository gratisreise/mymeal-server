package com.mymealserver.domain.food;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodWriter {

    private final FoodRepository foodRepository;

    public Food save(Food food) {
        return foodRepository.save(food);
    }
}
