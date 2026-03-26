package com.mymealserver.domain.food;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodReader {

  private final FoodRepository foodRepository;

  public Optional<Food> findByName(String name) {
    return foodRepository.findByName(name);
  }

  public Food findById(Long id) {
    return foodRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.FOOD_NOT_FOUND));
  }
}
