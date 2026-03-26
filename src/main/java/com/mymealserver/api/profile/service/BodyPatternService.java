package com.mymealserver.api.profile.service;

import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.dto.response.BodyPatternTagResponse;
import com.mymealserver.domain.foodmemberstats.FoodMemberStats;
import com.mymealserver.domain.foodmemberstats.FoodMemberStatsRepository;
import com.mymealserver.domain.reaction.ReactionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodyPatternService {

  private final FoodMemberStatsRepository foodMemberStatsRepository;
  private final ReactionRepository reactionRepository;

  private static final double GOOD_TAG_MIN_SCORE = 4.0; // 좋은 태그 최소 점수
  private static final double BAD_TAG_MAX_SCORE = 2.5; // 나쁜 태그 최대 점수
  private static final int MIN_MEAL_COUNT = 3; // 최소 식사 횟수

  // 회원의 신체 패턴 분석 결과 조회
  public BodyPatternResponse getBodyPatterns(Long memberId) {
    log.debug("신체 패턴 분석 조회: memberId={}", memberId);

    // 회원의 모든 음식 통계 조회
    List<FoodMemberStats> foodStats = foodMemberStatsRepository.findByMemberId(memberId);

    if (foodStats.isEmpty()) {
      return BodyPatternResponse.of(new ArrayList<>(), new ArrayList<>(), 0.0);
    }

    // 반응으로부터 전체 평균 점수 계산
    Double overallAverage = reactionRepository.calculateAverageScoreByMemberId(memberId);
    if (overallAverage == null) {
      overallAverage = 0.0;
    }

    // FoodMemberStats에서 태그 통계 조회
    List<Object[]> tagStats = foodMemberStatsRepository.findTagStatistics(memberId);

    // 태그를 좋은 태그와 나쁜 태그로 분류
    List<BodyPatternTagResponse> goodTags = new ArrayList<>();
    List<BodyPatternTagResponse> badTags = new ArrayList<>();

    for (Object[] row : tagStats) {
      String tagName = (String) row[1];
      double averageScore = ((Number) row[2]).doubleValue();
      int count = ((Number) row[3]).intValue();

      BodyPatternTagResponse tag = BodyPatternTagResponse.of(tagName, averageScore, count);

      // 좋은 태그: 평균 >= 4.0, 최소 3회 식사
      if (averageScore >= GOOD_TAG_MIN_SCORE && count >= MIN_MEAL_COUNT) {
        goodTags.add(tag);
      }

      // 나쁜 태그: 평균 < 2.5, 최소 3회 식사
      if (averageScore < BAD_TAG_MAX_SCORE && count >= MIN_MEAL_COUNT) {
        badTags.add(tag);
      }
    }

    // 정렬 후 상위 3개만 선택
    goodTags.sort(Comparator.comparing(BodyPatternTagResponse::averageScore).reversed());
    badTags.sort(Comparator.comparing(BodyPatternTagResponse::averageScore));

    List<BodyPatternTagResponse> topGoodTags = goodTags.stream().limit(3).toList();

    List<BodyPatternTagResponse> topBadTags = badTags.stream().limit(3).toList();
    Double overallAverageScore = Math.round(overallAverage * 100.0) / 100.0;

    return BodyPatternResponse.of(topGoodTags, topBadTags, overallAverageScore);
  }
}
