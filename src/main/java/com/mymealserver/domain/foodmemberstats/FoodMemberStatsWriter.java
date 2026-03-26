package com.mymealserver.domain.foodmemberstats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodMemberStatsWriter {

  private final FoodMemberStatsRepository foodMemberStatsRepository;

  @Transactional
  public FoodMemberStats save(FoodMemberStats foodMemberStats) {
    return foodMemberStatsRepository.save(foodMemberStats);
  }

  @Transactional
  public void delete(FoodMemberStats foodMemberStats) {
    foodMemberStatsRepository.delete(foodMemberStats);
  }
}
