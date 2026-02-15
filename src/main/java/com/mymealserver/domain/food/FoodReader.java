package com.mymealserver.domain.food;

import com.mymealserver.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodReader {

    private final FoodRepository foodRepository;

}
