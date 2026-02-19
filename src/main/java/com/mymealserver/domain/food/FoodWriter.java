package com.mymealserver.domain.food;

import com.mymealserver.entity.Food;
import com.mymealserver.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodWriter {

    private final FoodRepository foodRepository;

    /**
     * Food 저장
     */
    public Food save(Food food) {
        return foodRepository.save(food);
    }
}
