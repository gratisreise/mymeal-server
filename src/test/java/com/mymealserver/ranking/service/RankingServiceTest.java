package com.mymealserver.ranking.service;

import com.mymealserver.api.ranking.service.DateRange;
import com.mymealserver.api.ranking.service.RankingService;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.common.test.fixtures.RankingFixture;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.GradeType;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.api.ranking.dto.response.RankingItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingService 단위 테스트")
class RankingServiceTest {

    @Mock
    private MealReader mealReader;

    @Mock
    private ReactionReader reactionReader;

    @InjectMocks
    private RankingService rankingService;

    private Long testMemberId;
    private List<Meal> testMeals;
    private Map<Long, Reaction> testReactions;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;

        testMeals = RankingFixture.createMealsWithVariousScores();

        List<Reaction> reactions = RankingFixture.createReactionsWithScores();
        testReactions = Map.of(
                1L, reactions.get(0),
                2L, reactions.get(1),
                3L, reactions.get(2),
                4L, reactions.get(3),
                5L, reactions.get(4)
        );
    }

    @Nested
    @DisplayName("최고 식사 랭킹 조회")
    class GetBestRankingTests {

        @Test
        @DisplayName("최고 식사 랭킹 - 점수 높은 순으로 정렬")
        void getBestRanking_WithValidData_ShouldReturnHighestScoresFirst() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(testMeals, pageable, testMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);

            List<Double> scores = response.getData().stream()
                    .map(RankingItemResponse::overallScore)
                    .toList();
            assertThat(scores).isEqualTo(List.of(5.0, 4.5, 3.0, 2.0, 1.0));

            assertThat(response.getData().get(0).rank()).isEqualTo(1);
            assertThat(response.getData().get(0).overallScore()).isEqualTo(5.0);
            assertThat(response.getData().get(0).grade()).isEqualTo(GradeType.GOOD);

            assertThat(response.getData().get(4).rank()).isEqualTo(5);
            assertThat(response.getData().get(4).overallScore()).isEqualTo(1.0);
            assertThat(response.getData().get(4).grade()).isEqualTo(GradeType.BAD);
        }

        @Test
        @DisplayName("최고 식사 랭킹 - 식사 데이터 없음")
        void getBestRanking_WithNoMeals_ShouldReturnEmptyRanking() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> emptyPage = Page.empty();

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(emptyPage);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEmpty();
            assertThat(response.getPagination().getTotalElements()).isZero();
        }

        @Test
        @DisplayName("최고 식사 랭킹 - 반응 없는 식사는 제외")
        void getBestRanking_WithMealsWithoutReactions_ShouldExcludeThem() {
            // Given
            List<Meal> mealsWithAndWithoutReactions = new java.util.ArrayList<>(testMeals);
            mealsWithAndWithoutReactions.addAll(RankingFixture.createMealsWithoutReactions());

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(mealsWithAndWithoutReactions, pageable, mealsWithAndWithoutReactions.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions); // Only reactions for first 5 meals

            // When
            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);
            assertThat(response.getData()).allMatch(
                    item -> item.overallScore() != null,
                    "모든 항목에 점수가 있어야 함"
            );
        }

        @Test
        @DisplayName("최고 식사 랭킹 - 동점일 경우 mealTime 기준 최신순")
        void getBestRanking_WithSameScores_ShouldSortByMealTimeDesc() {
            // Given
            List<Meal> mealsWithSameScores = RankingFixture.createMealsWithSameScores();
            List<Reaction> reactionsWithSameScores = RankingFixture.createReactionsWithSameScores();

            Map<Long, Reaction> sameScoreReactions = Map.of(
                    40L, reactionsWithSameScores.get(0),
                    41L, reactionsWithSameScores.get(1),
                    42L, reactionsWithSameScores.get(2)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(mealsWithSameScores, pageable, mealsWithSameScores.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(sameScoreReactions);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(3);

            assertThat(response.getData()).allMatch(
                    item -> item.overallScore().equals(3.0),
                    "모든 항목 점수는 3.0이어야 함"
            );

            List<LocalDateTime> mealTimes = response.getData().stream()
                    .map(RankingItemResponse::mealTime)
                    .toList();
            assertThat(mealTimes).isSortedAccordingTo(Comparator.reverseOrder());
        }

        @Test
        @DisplayName("최고 식사 랭킹 - MealType 필터링")
        void getBestRanking_WithMealTypeFilter_ShouldReturnOnlyThatType() {
            // Given
            List<Meal> mealsByType = RankingFixture.createMealsByType();
            List<Reaction> reactionsByType = RankingFixture.createReactionsByType();

            Map<Long, Reaction> reactionsByTypeMap = Map.of(
                    11L, reactionsByType.get(1)  // LUNCH, score 3.5
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(List.of(mealsByType.get(1)), pageable, 1);

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), eq(MealType.LUNCH), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(reactionsByTypeMap);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, MealType.LUNCH, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getData().get(0).mealType()).isEqualTo(MealType.LUNCH);
        }
    }

    @Nested
    @DisplayName("최저 식사 랭킹 조회")
    class GetWorstRankingTests {

        @Test
        @DisplayName("최저 식사 랭킹 - 점수 낮은 순으로 정렬")
        void getWorstRanking_WithValidData_ShouldReturnLowestScoresFirst() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(testMeals, pageable, testMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getWorstRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);

            List<Double> scores = response.getData().stream()
                    .map(RankingItemResponse::overallScore)
                    .toList();
            assertThat(scores).isEqualTo(List.of(1.0, 2.0, 3.0, 4.5, 5.0));

            assertThat(response.getData().get(0).rank()).isEqualTo(1);
            assertThat(response.getData().get(0).overallScore()).isEqualTo(1.0);
            assertThat(response.getData().get(0).grade()).isEqualTo(GradeType.BAD);

            assertThat(response.getData().get(4).rank()).isEqualTo(5);
            assertThat(response.getData().get(4).overallScore()).isEqualTo(5.0);
            assertThat(response.getData().get(4).grade()).isEqualTo(GradeType.GOOD);
        }

        @Test
        @DisplayName("최저 식사 랭킹 - 식사 데이터 없음")
        void getWorstRanking_WithNoMeals_ShouldReturnEmptyRanking() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> emptyPage = Page.empty();

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(emptyPage);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getWorstRanking(
                    testMemberId, null, null, pageable
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEmpty();
            assertThat(response.getPagination().getTotalElements()).isZero();
        }

        @Test
        @DisplayName("최저 식사 랭킹 - 동점일 경우 mealTime 기준 최신순")
        void getWorstRanking_WithSameScores_ShouldSortByMealTimeDesc() {
            // Given
            List<Meal> mealsWithSameScores = RankingFixture.createMealsWithSameScores();
            List<Reaction> reactionsWithSameScores = RankingFixture.createReactionsWithSameScores();

            Map<Long, Reaction> sameScoreReactions = Map.of(
                    40L, reactionsWithSameScores.get(0),
                    41L, reactionsWithSameScores.get(1),
                    42L, reactionsWithSameScores.get(2)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(mealsWithSameScores, pageable, mealsWithSameScores.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(sameScoreReactions);

            // When
            PageResponse<RankingItemResponse> response = rankingService.getWorstRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(3);

            assertThat(response.getData()).allMatch(
                    item -> item.overallScore().equals(3.0),
                    "모든 항목 점수는 3.0이어야 함"
            );

            List<LocalDateTime> mealTimes = response.getData().stream()
                    .map(RankingItemResponse::mealTime)
                    .toList();
            assertThat(mealTimes).isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Nested
    @DisplayName("날짜 범위 필터링")
    class DateRangeFilteringTests {

        @Test
        @DisplayName("날짜 범위 필터링 - 시작일과 종료일 사이의 식사만 반환")
        void getBestRanking_WithDateRange_ShouldReturnMealsInRange() {
            // Given
            List<Meal> mealsForDateRange = RankingFixture.createMealsForDateRange();
            List<Reaction> reactionsForDateRange = RankingFixture.createReactionsForDateRange();

            Map<Long, Reaction> reactionsMap = Map.of(
                    31L, reactionsForDateRange.get(1)  // 2025-02-15, score 3.0
            );

            LocalDate startDate = LocalDate.of(2025, 2, 1);
            LocalDate endDate = LocalDate.of(2025, 2, 28);

            Pageable pageable = PageRequest.of(0, 10);

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), eq(endDate), isNull(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(mealsForDateRange.get(1)), pageable, 1));
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(reactionsMap);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, new DateRange(startDate, endDate), pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getData().get(0).mealTime()).isEqualTo(LocalDateTime.of(2025, 2, 15, 12, 0));
        }

        @Test
        @DisplayName("날짜 범위 필터링 - 시작일만 지정")
        void getBestRanking_WithStartDateOnly_ShouldReturnMealsFromDate() {
            LocalDate startDate = LocalDate.of(2025, 2, 1);
            Pageable pageable = PageRequest.of(0, 10);

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), isNull(), isNull(), eq(pageable)))
                    .thenReturn(new PageImpl<>(testMeals, pageable, testMeals.size()));
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, new DateRange(startDate, null), pageable
            );

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("날짜 범위 필터링 - 종료일만 지정")
        void getBestRanking_WithEndDateOnly_ShouldReturnMealsUntilDate() {
            LocalDate endDate = LocalDate.of(2025, 2, 28);
            Pageable pageable = PageRequest.of(0, 10);

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), eq(endDate), isNull(), eq(pageable)))
                    .thenReturn(new PageImpl<>(testMeals, pageable, testMeals.size()));
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, new DateRange(null, endDate), pageable
            );

            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("페이징")
    class PaginationTests {

        @Test
        @DisplayName("페이징 - 첫 번째 페이지")
        void getBestRanking_WithFirstPage_ShouldReturnCorrectPage() {
            List<Meal> largeMealSet = RankingFixture.createLargeMealSet();
            List<Reaction> largeReactionSet = RankingFixture.createLargeReactionSet();

            Map<Long, Reaction> largeReactionsMap = new java.util.HashMap<>();
            for (int i = 0; i < largeMealSet.size(); i++) {
                largeReactionsMap.put(largeMealSet.get(i).getId(), largeReactionSet.get(i));
            }

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(
                    largeMealSet.subList(0, 10),
                    pageable,
                    largeMealSet.size()
            );

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenAnswer(invocation -> {
                        List<Long> mealIds = invocation.getArgument(0);
                        return largeReactionsMap.entrySet().stream()
                                .filter(e -> mealIds.contains(e.getKey()))
                                .collect(java.util.stream.Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ));
                    });

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(10);
            assertThat(response.getPagination().getCurrentPage()).isEqualTo(1);
            assertThat(response.getPagination().getPageSize()).isEqualTo(10);
            assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
            assertThat(response.getPagination().getTotalElements()).isEqualTo(25);
        }

        @Test
        @DisplayName("페이징 - 두 번째 페이지")
        void getBestRanking_WithSecondPage_ShouldReturnCorrectPage() {
            List<Meal> largeMealSet = RankingFixture.createLargeMealSet();
            List<Reaction> largeReactionSet = RankingFixture.createLargeReactionSet();

            Map<Long, Reaction> largeReactionsMap = new java.util.HashMap<>();
            for (int i = 0; i < largeMealSet.size(); i++) {
                largeReactionsMap.put(largeMealSet.get(i).getId(), largeReactionSet.get(i));
            }

            Pageable pageable = PageRequest.of(1, 10);
            Page<Meal> mealPage = new PageImpl<>(
                    largeMealSet.subList(10, 20),
                    pageable,
                    largeMealSet.size()
            );

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenAnswer(invocation -> {
                        List<Long> mealIds = invocation.getArgument(0);
                        return largeReactionsMap.entrySet().stream()
                                .filter(e -> mealIds.contains(e.getKey()))
                                .collect(java.util.stream.Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ));
                    });

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(10);
            assertThat(response.getPagination().getCurrentPage()).isEqualTo(2);
        }

        @Test
        @DisplayName("페이징 - 마지막 페이지 (부분)")
        void getBestRanking_WithLastPage_ShouldReturnPartialPage() {
            List<Meal> largeMealSet = RankingFixture.createLargeMealSet();
            List<Reaction> largeReactionSet = RankingFixture.createLargeReactionSet();

            Map<Long, Reaction> largeReactionsMap = new java.util.HashMap<>();
            for (int i = 0; i < largeMealSet.size(); i++) {
                largeReactionsMap.put(largeMealSet.get(i).getId(), largeReactionSet.get(i));
            }

            Pageable pageable = PageRequest.of(2, 10);
            Page<Meal> mealPage = new PageImpl<>(
                    largeMealSet.subList(20, 25),
                    pageable,
                    largeMealSet.size()
            );

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenAnswer(invocation -> {
                        List<Long> mealIds = invocation.getArgument(0);
                        return largeReactionsMap.entrySet().stream()
                                .filter(e -> mealIds.contains(e.getKey()))
                                .collect(java.util.stream.Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ));
                    });

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);
            assertThat(response.getPagination().getCurrentPage()).isEqualTo(3);
        }

        @Test
        @DisplayName("페이징 - 페이지 크기 1")
        void getBestRanking_WithPageSizeOne_ShouldReturnSingleItem() {
            Pageable pageable = PageRequest.of(0, 1);
            Page<Meal> mealPage = new PageImpl<>(
                    List.of(testMeals.get(0)),
                    pageable,
                    testMeals.size()
            );

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(Map.of(1L, testReactions.get(1L)));

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getPagination().getPageSize()).isEqualTo(1);
            assertThat(response.getPagination().getTotalPages()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {

        @Test
        @DisplayName("경계값 - 최대 점수 (5.0)")
        void getBestRanking_WithMaxScore_ShouldHandleCorrectly() {
            List<Meal> boundaryMeals = RankingFixture.createBoundaryScoreMeals();
            List<Reaction> boundaryReactions = RankingFixture.createBoundaryReactions();

            Map<Long, Reaction> boundaryReactionsMap = Map.of(
                    20L, boundaryReactions.get(0),
                    21L, boundaryReactions.get(1),
                    22L, boundaryReactions.get(2)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(boundaryMeals, pageable, boundaryMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(boundaryReactionsMap);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(3);
            assertThat(response.getData().get(0).overallScore()).isEqualTo(5.0);
            assertThat(response.getData().get(0).grade()).isEqualTo(GradeType.GOOD);
        }

        @Test
        @DisplayName("경계값 - 최소 점수 (1.0)")
        void getWorstRanking_WithMinScore_ShouldHandleCorrectly() {
            List<Meal> boundaryMeals = RankingFixture.createBoundaryScoreMeals();
            List<Reaction> boundaryReactions = RankingFixture.createBoundaryReactions();

            Map<Long, Reaction> boundaryReactionsMap = Map.of(
                    20L, boundaryReactions.get(0),
                    21L, boundaryReactions.get(1),
                    22L, boundaryReactions.get(2)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(boundaryMeals, pageable, boundaryMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(boundaryReactionsMap);

            PageResponse<RankingItemResponse> response = rankingService.getWorstRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(3);
            assertThat(response.getData().get(0).overallScore()).isEqualTo(1.0);
            assertThat(response.getData().get(0).grade()).isEqualTo(GradeType.BAD);
        }

        @Test
        @DisplayName("경계값 - 페이지 크기가 총 데이터보다 큼")
        void getBestRanking_WithPageSizeLargerThanData_ShouldReturnAllData() {
            Pageable pageable = PageRequest.of(0, 100);
            Page<Meal> mealPage = new PageImpl<>(testMeals, pageable, testMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);
            assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("경계값 - 등급별 필터링 확인")
        void getBestRanking_WithVariousGrades_ShouldClassifyCorrectly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> mealPage = new PageImpl<>(testMeals, pageable, testMeals.size());

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(testReactions);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, null, pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(5);

            assertThat(response.getData().get(0).grade()).isEqualTo(GradeType.GOOD);
            assertThat(response.getData().get(1).grade()).isEqualTo(GradeType.GOOD);
            assertThat(response.getData().get(2).grade()).isEqualTo(GradeType.NORMAL);
            assertThat(response.getData().get(3).grade()).isEqualTo(GradeType.NORMAL);
            assertThat(response.getData().get(4).grade()).isEqualTo(GradeType.BAD);
        }
    }

    @Nested
    @DisplayName("복합 필터링")
    class CombinedFilteringTests {

        @Test
        @DisplayName("복합 필터링 - MealType + 날짜 범위")
        void getBestRanking_WithMealTypeAndDateRange_ShouldReturnFilteredResults() {
            List<Meal> mealsByType = RankingFixture.createMealsByType();
            List<Reaction> reactionsByType = RankingFixture.createReactionsByType();

            Map<Long, Reaction> reactionsMap = Map.of(
                    10L, reactionsByType.get(0)
            );

            LocalDate startDate = LocalDate.of(2025, 2, 1);
            LocalDate endDate = LocalDate.of(2025, 2, 28);

            Pageable pageable = PageRequest.of(0, 10);

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), eq(endDate), eq(MealType.BREAKFAST), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(mealsByType.get(0)), pageable, 1));
            when(reactionReader.findByMealIdsAsMap(any()))
                    .thenReturn(reactionsMap);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, MealType.BREAKFAST, new DateRange(startDate, endDate), pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getData().get(0).mealType()).isEqualTo(MealType.BREAKFAST);
        }

        @Test
        @DisplayName("복합 필터링 - 필터 결과 없음")
        void getBestRanking_WithFiltersThatMatchNothing_ShouldReturnEmpty() {
            LocalDate startDate = LocalDate.of(2025, 12, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Meal> emptyPage = Page.empty();

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), eq(endDate), isNull(), eq(pageable)))
                    .thenReturn(emptyPage);

            PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                    testMemberId, null, new DateRange(startDate, endDate), pageable
            );

            assertThat(response).isNotNull();
            assertThat(response.getData()).isEmpty();
        }
    }
}
