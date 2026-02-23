package com.mymealserver.domain.foodmemberstats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodMemberStatsReader {

    private final FoodMemberStatsRepository foodMemberStatsRepository;

    public FoodMemberStats findById(Long id) {
        return foodMemberStatsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FoodMemberStats not found"));
    }
}
