package api.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.service.BodyPatternService;
import com.mymealserver.domain.foodmemberstats.FoodMemberStats;
import com.mymealserver.domain.foodmemberstats.FoodMemberStatsRepository;
import com.mymealserver.domain.reaction.ReactionRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BodyPatternServiceTest {

  @Mock private FoodMemberStatsRepository foodMemberStatsRepository;
  @Mock private ReactionRepository reactionRepository;

  @InjectMocks private BodyPatternService bodyPatternService;

  private static final Long MEMBER_ID = 1L;

  // ========================
  // getBodyPatterns
  // ========================

  @Test
  void getBodyPatterns_success_noStats() {
    // given
    given(foodMemberStatsRepository.findByMemberId(MEMBER_ID)).willReturn(Collections.emptyList());

    // when
    BodyPatternResponse response = bodyPatternService.getBodyPatterns(MEMBER_ID);

    // then
    assertThat(response.goodTags()).isEmpty();
    assertThat(response.badTags()).isEmpty();
    assertThat(response.overallAverageScore()).isEqualTo(0.0);
  }

  @Test
  void getBodyPatterns_success_withGoodAndBadTags() {
    // given
    List<FoodMemberStats> foodStats = List.of(
        createFoodMemberStats(1L, MEMBER_ID, 4.5, 5),
        createFoodMemberStats(2L, MEMBER_ID, 2.0, 4)
    );
    given(foodMemberStatsRepository.findByMemberId(MEMBER_ID)).willReturn(foodStats);
    given(reactionRepository.calculateAverageScoreByMemberId(MEMBER_ID)).willReturn(3.456);

    List<Object[]> tagStats = List.of(
        new Object[]{MEMBER_ID, "샐러드", 4.5, 5},    // 좋은 태그 (>=4.0, count>=3)
        new Object[]{MEMBER_ID, "피자", 2.0, 4},      // 나쁜 태그 (<2.5, count>=3)
        new Object[]{MEMBER_ID, "밥", 3.5, 10}         // 해당 없음
    );
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(tagStats);

    // when
    BodyPatternResponse response = bodyPatternService.getBodyPatterns(MEMBER_ID);

    // then
    assertThat(response.goodTags()).hasSize(1);
    assertThat(response.goodTags().get(0).tag()).isEqualTo("샐러드");
    assertThat(response.goodTags().get(0).averageScore()).isEqualTo(4.5);

    assertThat(response.badTags()).hasSize(1);
    assertThat(response.badTags().get(0).tag()).isEqualTo("피자");
    assertThat(response.badTags().get(0).averageScore()).isEqualTo(2.0);

    assertThat(response.overallAverageScore()).isEqualTo(3.46); // 반올림
  }

  @Test
  void getBodyPatterns_success_nullOverallAverage() {
    // given
    List<FoodMemberStats> foodStats = List.of(
        createFoodMemberStats(1L, MEMBER_ID, 3.0, 3)
    );
    given(foodMemberStatsRepository.findByMemberId(MEMBER_ID)).willReturn(foodStats);
    given(reactionRepository.calculateAverageScoreByMemberId(MEMBER_ID)).willReturn(null);
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(Collections.emptyList());

    // when
    BodyPatternResponse response = bodyPatternService.getBodyPatterns(MEMBER_ID);

    // then
    assertThat(response.overallAverageScore()).isEqualTo(0.0);
  }

  @Test
  void getBodyPatterns_success_belowMinMealCountExcluded() {
    // given
    List<FoodMemberStats> foodStats = List.of(
        createFoodMemberStats(1L, MEMBER_ID, 4.8, 2),  // count < 3
        createFoodMemberStats(2L, MEMBER_ID, 1.5, 1)   // count < 3
    );
    given(foodMemberStatsRepository.findByMemberId(MEMBER_ID)).willReturn(foodStats);
    given(reactionRepository.calculateAverageScoreByMemberId(MEMBER_ID)).willReturn(3.0);

    List<Object[]> tagStats = List.of(
        new Object[]{MEMBER_ID, "초밥", 4.8, 2},   // 좋은 점수지만 count < 3
        new Object[]{MEMBER_ID, "라면", 1.5, 1}    // 나쁜 점수지만 count < 3
    );
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(tagStats);

    // when
    BodyPatternResponse response = bodyPatternService.getBodyPatterns(MEMBER_ID);

    // then
    assertThat(response.goodTags()).isEmpty(); // 최소 식사 횟수 미달
    assertThat(response.badTags()).isEmpty();
  }

  @Test
  void getBodyPatterns_success_top3TagsOnly() {
    // given
    List<FoodMemberStats> foodStats = List.of(
        createFoodMemberStats(1L, MEMBER_ID, 4.5, 5),
        createFoodMemberStats(2L, MEMBER_ID, 4.3, 4),
        createFoodMemberStats(3L, MEMBER_ID, 4.1, 3),
        createFoodMemberStats(4L, MEMBER_ID, 4.8, 6),  // 4번째 좋은 태그
        createFoodMemberStats(5L, MEMBER_ID, 2.0, 5),
        createFoodMemberStats(6L, MEMBER_ID, 2.2, 4),
        createFoodMemberStats(7L, MEMBER_ID, 1.5, 3),
        createFoodMemberStats(8L, MEMBER_ID, 1.8, 6)   // 4번째 나쁜 태그
    );
    given(foodMemberStatsRepository.findByMemberId(MEMBER_ID)).willReturn(foodStats);
    given(reactionRepository.calculateAverageScoreByMemberId(MEMBER_ID)).willReturn(3.0);

    List<Object[]> tagStats = List.of(
        new Object[]{MEMBER_ID, "샐러드", 4.8, 6},
        new Object[]{MEMBER_ID, "과일", 4.5, 5},
        new Object[]{MEMBER_ID, "요거트", 4.3, 4},
        new Object[]{MEMBER_ID, "현미밥", 4.1, 3},
        new Object[]{MEMBER_ID, "피자", 1.5, 3},
        new Object[]{MEMBER_ID, "햄버거", 1.8, 6},
        new Object[]{MEMBER_ID, "튀김", 2.0, 5},
        new Object[]{MEMBER_ID, "라면", 2.2, 4}
    );
    given(foodMemberStatsRepository.findTagStatistics(MEMBER_ID)).willReturn(tagStats);

    // when
    BodyPatternResponse response = bodyPatternService.getBodyPatterns(MEMBER_ID);

    // then
    assertThat(response.goodTags()).hasSize(3); // 최대 3개
    assertThat(response.badTags()).hasSize(3);  // 최대 3개

    // 좋은 태그는 점수 높은 순
    assertThat(response.goodTags().get(0).tag()).isEqualTo("샐러드");
    assertThat(response.goodTags().get(0).averageScore()).isEqualTo(4.8);

    // 나쁜 태그는 점수 낮은 순
    assertThat(response.badTags().get(0).tag()).isEqualTo("피자");
    assertThat(response.badTags().get(0).averageScore()).isEqualTo(1.5);
  }

  // --- Helper ---

  private FoodMemberStats createFoodMemberStats(Long id, Long memberId, double avgScore, int mealCount) {
    return FoodMemberStats.builder()
        .id(id)
        .memberId(memberId)
        .foodId(id * 100)
        .averageScore(avgScore)
        .mealCount(mealCount)
        .build();
  }
}
