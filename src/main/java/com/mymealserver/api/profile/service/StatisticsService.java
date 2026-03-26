package com.mymealserver.api.profile.service;

import com.mymealserver.api.profile.dto.response.StatisticsResponse;
import com.mymealserver.api.profile.dto.response.TagCountResponse;
import com.mymealserver.api.profile.dto.response.WeeklyTrendResponse;
import com.mymealserver.domain.foodmemberstats.FoodMemberStatsRepository;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealRepository;
import com.mymealserver.domain.reaction.ReactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private final MealRepository mealRepository;
  private final ReactionRepository reactionRepository;
  private final FoodMemberStatsRepository foodMemberStatsRepository;

  public StatisticsResponse getStatistics(Long memberId) {
    log.debug("회원 통계 조회: memberId={}", memberId);

    // 전체 식사 수
    long totalMealCount = mealRepository.countByMemberIdAndDeletedAtIsNull(memberId);

    // 반응 등록률 계산
    Double reactionRate = calculateReactionRate(memberId, totalMealCount);

    // 상위 태그 조회
    List<TagCountResponse> topTags = getTopTags(memberId);

    // 주간 트렌드 (최근 7일)
    List<WeeklyTrendResponse> weeklyTrend = getWeeklyTrend(memberId);

    return StatisticsResponse.of((int) totalMealCount, reactionRate, topTags, weeklyTrend);
  }

  private Double calculateReactionRate(Long memberId, long totalMealCount) {
    if (totalMealCount == 0) {
      return 0.0;
    }

    // 넓은 범위로 식사 조회
    List<Meal> meals =
        mealRepository.findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
            memberId, LocalDateTime.now().minusYears(100), LocalDateTime.now());

    if (meals.isEmpty()) {
      return 0.0;
    }

    // 식사 ID 목록 추출
    List<Long> mealIds = meals.stream().map(Meal::getId).toList();

    // 반응 수 카운트
    long reactionCount = reactionRepository.countByMealIdIn(mealIds);

    return (reactionCount * 100.0) / totalMealCount;
  }

  private List<TagCountResponse> getTopTags(Long memberId) {
    log.debug("상위 태그 조회: memberId={}", memberId);

    try {
      // 회원의 음식 통계 조회
      List<Object[]> tagStats = foodMemberStatsRepository.findTagStatistics(memberId);

      // TagCountResponse로 변환 후 상위 5개 제한
      return tagStats.stream()
          .limit(5)
          .map(
              row ->
                  TagCountResponse.of(
                      (String) row[1], // 태그 이름 (음식 이름을 대용으로 사용)
                      ((Number) row[3]).intValue() // 식사 횟수
                      ))
          .toList();

    } catch (Exception e) {
      log.warn("상위 태그 조회 실패: memberId={}, 빈 목록 반환", memberId, e);
      return new ArrayList<>();
    }
  }

  private List<WeeklyTrendResponse> getWeeklyTrend(Long memberId) {
    log.debug("주간 트렌드 조회: memberId={}", memberId);

    try {
      LocalDateTime startDate = LocalDateTime.now().minusDays(7);
      List<Object[]> results = mealRepository.findWeeklyTrend(memberId, startDate);

      return results.stream()
          .map(
              row ->
                  WeeklyTrendResponse.of(
                      (LocalDate) row[0], // 날짜
                      ((Number) row[1]).doubleValue() // 평균 점수
                      ))
          .toList();

    } catch (Exception e) {
      log.warn("주간 트렌드 조회 실패: memberId={}, 빈 목록 반환", memberId, e);
      return new ArrayList<>();
    }
  }
}
