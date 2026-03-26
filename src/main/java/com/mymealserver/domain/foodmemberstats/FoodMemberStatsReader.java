package com.mymealserver.domain.foodmemberstats;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodMemberStatsReader {

  private final FoodMemberStatsRepository foodMemberStatsRepository;

  public FoodMemberStats findById(Long id) {
    return foodMemberStatsRepository
        .findById(id)
        .orElseThrow(() -> BusinessException.error(ErrorCode.FOOD_MEMBER_STATS_NOT_FOUND));
  }
}
