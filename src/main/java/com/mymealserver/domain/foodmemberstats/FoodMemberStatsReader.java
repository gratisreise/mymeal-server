package com.mymealserver.domain.FoodMemberStats;

import com.mymealserver.entity.Food;
import com.mymealserver.entity.FoodMemberStats;
import com.mymealserver.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodMemberStatsReader {

    private final FoodMemberStatsRepository foodMemberStatsRepository;

    public FoodMemberStats findById(Long id) {
        return foodMemberStatsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FoodMemberStats not found"));
    }

    public Optional<FoodMemberStats> findByMemberAndFood(Member member, Food food) {
        return foodMemberStatsRepository.findByMemberAndFood(member, food);
    }
}
