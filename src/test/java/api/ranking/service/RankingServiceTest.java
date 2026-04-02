package api.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.mymealserver.api.ranking.dto.response.RankingItemResponse;
import com.mymealserver.api.ranking.service.DateRange;
import com.mymealserver.api.ranking.service.RankingService;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

  @Mock private MealReader mealReader;
  @Mock private ReactionReader reactionReader;

  @InjectMocks private RankingService rankingService;

  private static final Long MEMBER_ID = 1L;

  // ========================
  // getBestRanking
  // ========================

  @Test
  void getBestRanking_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    Meal meal2 = createMeal(2L, MealType.DINNER, LocalDateTime.of(2025, 3, 2, 18, 0));
    Meal meal3 = createMeal(3L, MealType.BREAKFAST, LocalDateTime.of(2025, 3, 3, 8, 0));
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1, meal2, meal3), pageable, 3));

    Reaction reaction1 = createReaction(1L, 4.5, GradeType.GOOD);
    Reaction reaction2 = createReaction(2L, 2.0, GradeType.BAD);
    Reaction reaction3 = createReaction(3L, 3.5, GradeType.NORMAL);
    given(reactionReader.findByMealIdsAsMap(anyList()))
        .willReturn(Map.of(1L, reaction1, 2L, reaction2, 3L, reaction3));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).hasSize(3);
    // 점수 내림차순: 4.5 > 3.5 > 2.0
    assertThat(result.getData().get(0).overallScore()).isEqualTo(4.5);
    assertThat(result.getData().get(0).rank()).isEqualTo(1);
    assertThat(result.getData().get(1).overallScore()).isEqualTo(3.5);
    assertThat(result.getData().get(1).rank()).isEqualTo(2);
    assertThat(result.getData().get(2).overallScore()).isEqualTo(2.0);
    assertThat(result.getData().get(2).rank()).isEqualTo(3);
  }

  @Test
  void getBestRanking_success_emptyMeals() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).isEmpty();
    assertThat(result.getPagination().totalElements()).isZero();
  }

  @Test
  void getBestRanking_success_noReactions() {
    // given: 식사는 있지만 반응이 없는 경우 → 필터링되어 빈 결과
    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1), pageable, 1));

    given(reactionReader.findByMealIdsAsMap(anyList())).willReturn(Map.of());

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).isEmpty();
  }

  @Test
  void getBestRanking_success_sameScoreRanking() {
    // given: 동점인 경우 최신 식사순 정렬 + 동일 순위
    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    Meal meal2 = createMeal(2L, MealType.DINNER, LocalDateTime.of(2025, 3, 3, 18, 0));
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1, meal2), pageable, 2));

    Reaction reaction1 = createReaction(1L, 4.0, GradeType.GOOD);
    Reaction reaction2 = createReaction(2L, 4.0, GradeType.GOOD);
    given(reactionReader.findByMealIdsAsMap(anyList()))
        .willReturn(Map.of(1L, reaction1, 2L, reaction2));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).hasSize(2);
    // 동점 → 최신 식사(meal2, 3/3)가 먼저
    assertThat(result.getData().get(0).mealId()).isEqualTo(2L);
    assertThat(result.getData().get(0).rank()).isEqualTo(1);
    assertThat(result.getData().get(1).mealId()).isEqualTo(1L);
    assertThat(result.getData().get(1).rank()).isEqualTo(1); // 동점 → 같은 순위
  }

  @Test
  void getBestRanking_success_withDateRange() {
    // given
    LocalDate start = LocalDate.of(2025, 3, 1);
    LocalDate end = LocalDate.of(2025, 3, 31);
    DateRange dateRange = DateRange.of(start, end);
    Pageable pageable = PageRequest.of(0, 10);

    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 15, 12, 0));
    given(mealReader.findByMemberId(eq(MEMBER_ID), eq(start), eq(end), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1), pageable, 1));

    Reaction reaction1 = createReaction(1L, 4.2, GradeType.GOOD);
    given(reactionReader.findByMealIdsAsMap(anyList())).willReturn(Map.of(1L, reaction1));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, null, dateRange, pageable);

    // then
    assertThat(result.getData()).hasSize(1);
    assertThat(result.getData().get(0).overallScore()).isEqualTo(4.2);
  }

  @Test
  void getBestRanking_success_withMealType() {
    // given
    Pageable pageable = PageRequest.of(0, 10);

    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), eq(MealType.LUNCH), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1), pageable, 1));

    Reaction reaction1 = createReaction(1L, 3.8, GradeType.NORMAL);
    given(reactionReader.findByMealIdsAsMap(anyList())).willReturn(Map.of(1L, reaction1));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getBestRanking(MEMBER_ID, MealType.LUNCH, null, pageable);

    // then
    assertThat(result.getData()).hasSize(1);
    assertThat(result.getData().get(0).mealType()).isEqualTo(MealType.LUNCH);
  }

  // ========================
  // getWorstRanking
  // ========================

  @Test
  void getWorstRanking_success() {
    // given
    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    Meal meal2 = createMeal(2L, MealType.DINNER, LocalDateTime.of(2025, 3, 2, 18, 0));
    Meal meal3 = createMeal(3L, MealType.BREAKFAST, LocalDateTime.of(2025, 3, 3, 8, 0));
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1, meal2, meal3), pageable, 3));

    Reaction reaction1 = createReaction(1L, 4.5, GradeType.GOOD);
    Reaction reaction2 = createReaction(2L, 2.0, GradeType.BAD);
    Reaction reaction3 = createReaction(3L, 3.5, GradeType.NORMAL);
    given(reactionReader.findByMealIdsAsMap(anyList()))
        .willReturn(Map.of(1L, reaction1, 2L, reaction2, 3L, reaction3));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getWorstRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).hasSize(3);
    // 점수 오름차순: 2.0 < 3.5 < 4.5
    assertThat(result.getData().get(0).overallScore()).isEqualTo(2.0);
    assertThat(result.getData().get(0).rank()).isEqualTo(1);
    assertThat(result.getData().get(1).overallScore()).isEqualTo(3.5);
    assertThat(result.getData().get(1).rank()).isEqualTo(2);
    assertThat(result.getData().get(2).overallScore()).isEqualTo(4.5);
    assertThat(result.getData().get(2).rank()).isEqualTo(3);
  }

  @Test
  void getWorstRanking_success_emptyMeals() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getWorstRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).isEmpty();
    assertThat(result.getPagination().totalElements()).isZero();
  }

  @Test
  void getWorstRanking_success_sameScoreRanking() {
    // given: 동점인 경우 최신 식사순 + 동일 순위
    Meal meal1 = createMeal(1L, MealType.LUNCH, LocalDateTime.of(2025, 3, 1, 12, 0));
    Meal meal2 = createMeal(2L, MealType.DINNER, LocalDateTime.of(2025, 3, 5, 18, 0));
    Meal meal3 = createMeal(3L, MealType.BREAKFAST, LocalDateTime.of(2025, 3, 3, 8, 0));
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(eq(MEMBER_ID), any(), any(), any(), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(meal1, meal2, meal3), pageable, 3));

    Reaction reaction1 = createReaction(1L, 2.0, GradeType.BAD);
    Reaction reaction2 = createReaction(2L, 2.0, GradeType.BAD);
    Reaction reaction3 = createReaction(3L, 3.0, GradeType.NORMAL);
    given(reactionReader.findByMealIdsAsMap(anyList()))
        .willReturn(Map.of(1L, reaction1, 2L, reaction2, 3L, reaction3));

    // when
    PageResponse<RankingItemResponse> result =
        rankingService.getWorstRanking(MEMBER_ID, null, null, pageable);

    // then
    assertThat(result.getData()).hasSize(3);
    // 동점(2.0) → 최신 식사(meal2, 3/5) 먼저
    assertThat(result.getData().get(0).mealId()).isEqualTo(2L);
    assertThat(result.getData().get(0).rank()).isEqualTo(1);
    assertThat(result.getData().get(1).mealId()).isEqualTo(1L);
    assertThat(result.getData().get(1).rank()).isEqualTo(1);
    // 다른 점수(3.0) → 순위 3
    assertThat(result.getData().get(2).mealId()).isEqualTo(3L);
    assertThat(result.getData().get(2).rank()).isEqualTo(3);
  }

  // --- Helper ---

  private Meal createMeal(Long id, MealType mealType, LocalDateTime mealTime) {
    return Meal.builder()
        .id(id)
        .memberId(MEMBER_ID)
        .mealType(mealType)
        .mealTime(mealTime)
        .photoUrl("https://example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .build();
  }

  private Reaction createReaction(Long mealId, Double overallScore, GradeType grade) {
    return Reaction.builder()
        .id(mealId * 100)
        .mealId(mealId)
        .digestionLevel((short) 3)
        .fullnessLevel((short) 3)
        .energyLevel((short) 3)
        .overallScore(overallScore)
        .grade(grade)
        .build();
  }
}
