package com.mymealserver.domain.food;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodReader {

    private final FoodRepository foodRepository;

    /**
     * 음식 이름으로 조회
     */
    public Optional<Food> findByName(String name) {
        return foodRepository.findByName(name);
    }

    /**
     * ID로 음식 조회
     */
    public Food findById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOOD_NOT_FOUND));
    }
}
