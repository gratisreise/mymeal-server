package com.mymealserver.profile.service;

import com.mymealserver.api.profile.service.BodyPatternService;
import com.mymealserver.domain.foodmemberstats.FoodMemberStats;
import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.domain.foodmemberstats.FoodMemberStatsRepository;
import com.mymealserver.domain.reaction.ReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BodyPatternService 단위 테스트")
class BodyPatternServiceTest {

    @Mock
    private FoodMemberStatsRepository foodMemberStatsRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @InjectMocks
    private BodyPatternService bodyPatternService;

    private Long testMemberId;
    private List<FoodMemberStats> testFoodStats;

    // Thresholds (matching service constants)
    private static final double GOOD_TAG_MIN_SCORE = 4.0;
    private static final double BAD_TAG_MAX_SCORE = 2.5;
    private static final int MIN_MEAL_COUNT = 3;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;

        // Create test food stats
        testFoodStats = List.of(
                createFoodStats(1L, 1L, 4.5, 5),
                createFoodStats(2L, 2L, 4.2, 4),
                createFoodStats(3L, 3L, 4.0, 3),  // Boundary: exactly 4.0
                createFoodStats(4L, 4L, 3.8, 2),  // Below min count
                createFoodStats(5L, 5L, 2.4, 4),  // Bad tag boundary
                createFoodStats(6L, 6L, 2.5, 5),  // Boundary: exactly 2.5 (not bad)
                createFoodStats(7L, 7L, 1.5, 3),  // Bad tag
                createFoodStats(8L, 8L, 2.0, 2)   // Below min count
        );
    }

    private FoodMemberStats createFoodStats(Long id, Long foodId, double avgScore, int mealCount) {
        return FoodMemberStats.builder()
                .id(id)
                .memberId(testMemberId)
                .foodId(foodId)
                .averageScore(avgScore)
                .mealCount(mealCount)
                .lastMealAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("바디 패턴 분석")
    class GetBodyPatternsTests {

        @Test
        @DisplayName("좋은 태그와 나쁜 태그를 올바르게 분류한다")
        void getBodyPatterns_WithValidData_ShouldClassifyTagsCorrectly() {
            // Given
            // Mock tag statistics: [foodId, tagName, avgScore, mealCount]
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Chicken", 4.5, 5},    // Good (>= 4.0, >= 3)
                    new Object[]{2L, "Rice", 4.2, 4},         // Good (>= 4.0, >= 3)
                    new Object[]{3L, "Salad", 4.0, 3},        // Good (>= 4.0, >= 3) boundary
                    new Object[]{4L, "Soup", 3.8, 2},         // Not enough count
                    new Object[]{5L, "Spicy", 2.4, 4},        // Bad (< 2.5, >= 3)
                    new Object[]{6L, "Greasy", 2.5, 5},       // Boundary (not bad, >= 2.5)
                    new Object[]{7L, "Salty", 1.5, 3}         // Bad (< 2.5, >= 3)
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.7);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.goodTags()).hasSize(3);
            assertThat(response.badTags()).hasSize(2);

            // Verify good tags (sorted descending by score)
            assertThat(response.goodTags().get(0).tag()).isEqualTo("Chicken");
            assertThat(response.goodTags().get(0).averageScore()).isEqualTo(4.5);
            assertThat(response.goodTags().get(1).tag()).isEqualTo("Rice");
            assertThat(response.goodTags().get(1).averageScore()).isEqualTo(4.2);
            assertThat(response.goodTags().get(2).tag()).isEqualTo("Salad");
            assertThat(response.goodTags().get(2).averageScore()).isEqualTo(4.0);

            // Verify bad tags (sorted ascending by score)
            assertThat(response.badTags().get(0).tag()).isEqualTo("Salty");
            assertThat(response.badTags().get(0).averageScore()).isEqualTo(1.5);
            assertThat(response.badTags().get(1).tag()).isEqualTo("Spicy");
            assertThat(response.badTags().get(1).averageScore()).isEqualTo(2.4);

            // Verify overall average
            assertThat(response.overallAverageScore()).isEqualTo(3.7);
        }

        @Test
        @DisplayName("식사 통계가 없는 회원의 빈 응답을 반환한다")
        void getBodyPatterns_WithNoFoodStats_ShouldReturnEmptyResponse() {
            // Given
            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(List.of());

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.goodTags()).isEmpty();
            assertThat(response.badTags()).isEmpty();
            assertThat(response.overallAverageScore()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("반응 기록이 없는 경우 전체 평균이 0.0이다")
        void getBodyPatterns_WithNoReactions_ShouldReturnZeroAverage() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Chicken", 4.5, 5}
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(null);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.overallAverageScore()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("최소 회수 미만 태그는 좋은/나쁜 태그 모두 제외된다")
        void getBodyPatterns_WithLowCountTags_ShouldExcludeFromBothCategories() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "HighScoreLowCount", 4.8, 2},  // Good score but count < 3
                    new Object[]{2L, "LowScoreLowCount", 2.0, 2},   // Bad score but count < 3
                    new Object[]{3L, "GoodTag", 4.5, 5},            // Good (>= 4.0, >= 3)
                    new Object[]{4L, "BadTag", 2.0, 4}              // Bad (< 2.5, >= 3)
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.5);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).hasSize(1);
            assertThat(response.badTags()).hasSize(1);
            assertThat(response.goodTags().get(0).tag()).isEqualTo("GoodTag");
            assertThat(response.badTags().get(0).tag()).isEqualTo("BadTag");
        }

        @Test
        @DisplayName("경계값 테스트: 평균 4.0점은 좋은 태그에 포함된다")
        void getBodyPatterns_BoundaryTest_4_0_ShouldBeGoodTag() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "BoundaryTag", 4.0, 5}  // Exactly 4.0
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.5);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).hasSize(1);
            assertThat(response.goodTags().get(0).tag()).isEqualTo("BoundaryTag");
            assertThat(response.goodTags().get(0).averageScore()).isEqualTo(4.0);
            assertThat(response.badTags()).isEmpty();
        }

        @Test
        @DisplayName("경계값 테스트: 평균 2.5점은 나쁜 태그에 미포함된다")
        void getBodyPatterns_BoundaryTest_2_5_ShouldNotBeBadTag() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "BoundaryTag", 2.5, 5}  // Exactly 2.5
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.5);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).isEmpty();  // 2.5 < 4.0
            assertThat(response.badTags()).isEmpty();   // 2.5 is not < 2.5
        }

        @Test
        @DisplayName("좋은 태그가 3개 초과 시 top 3만 반환된다")
        void getBodyPatterns_WithMoreThan3GoodTags_ShouldReturnTop3() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Tag1", 5.0, 5},
                    new Object[]{2L, "Tag2", 4.8, 4},
                    new Object[]{3L, "Tag3", 4.5, 6},
                    new Object[]{4L, "Tag4", 4.2, 3},
                    new Object[]{5L, "Tag5", 4.1, 5}
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(4.0);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).hasSize(3);
            assertThat(response.goodTags().get(0).tag()).isEqualTo("Tag1");
            assertThat(response.goodTags().get(1).tag()).isEqualTo("Tag2");
            assertThat(response.goodTags().get(2).tag()).isEqualTo("Tag3");
        }

        @Test
        @DisplayName("나쁜 태그가 3개 초과 시 top 3만 반환된다")
        void getBodyPatterns_WithMoreThan3BadTags_ShouldReturnTop3() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Bad1", 1.0, 5},
                    new Object[]{2L, "Bad2", 1.5, 4},
                    new Object[]{3L, "Bad3", 2.0, 6},
                    new Object[]{4L, "Bad4", 2.3, 3},
                    new Object[]{5L, "Bad5", 2.4, 5}
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(2.0);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.badTags()).hasSize(3);
            assertThat(response.badTags().get(0).tag()).isEqualTo("Bad1");
            assertThat(response.badTags().get(1).tag()).isEqualTo("Bad2");
            assertThat(response.badTags().get(2).tag()).isEqualTo("Bad3");
        }

        @Test
        @DisplayName("좋은 태그가 내림차순으로 정렬된다")
        void getBodyPatterns_GoodTags_ShouldBeSortedDescending() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Low", 4.1, 3},
                    new Object[]{2L, "High", 4.9, 5},
                    new Object[]{3L, "Mid", 4.5, 4}
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(4.0);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).hasSize(3);
            assertThat(response.goodTags().get(0).averageScore()).isEqualTo(4.9);
            assertThat(response.goodTags().get(1).averageScore()).isEqualTo(4.5);
            assertThat(response.goodTags().get(2).averageScore()).isEqualTo(4.1);
        }

        @Test
        @DisplayName("나쁜 태그가 오름차순으로 정렬된다")
        void getBodyPatterns_BadTags_ShouldBeSortedAscending() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Mid", 2.0, 3},
                    new Object[]{2L, "Low", 1.0, 5},
                    new Object[]{3L, "High", 2.4, 4}
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(2.0);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.badTags()).hasSize(3);
            assertThat(response.badTags().get(0).averageScore()).isEqualTo(1.0);
            assertThat(response.badTags().get(1).averageScore()).isEqualTo(2.0);
            assertThat(response.badTags().get(2).averageScore()).isEqualTo(2.4);
        }

        @Test
        @DisplayName("전체 평균이 소수점 둘째 자리로 반올림된다")
        void getBodyPatterns_OverallAverage_ShouldBeRoundedToTwoDecimals() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "Tag", 4.5, 5}
            );

            // Test various rounding scenarios
            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // Test 1: Round up (3.456 -> 3.46)
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.456);
            BodyPatternResponse response1 = bodyPatternService.getBodyPatterns(testMemberId);
            assertThat(response1.overallAverageScore()).isEqualTo(3.46);

            // Test 2: Round down (3.454 -> 3.45)
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.454);
            BodyPatternResponse response2 = bodyPatternService.getBodyPatterns(testMemberId);
            assertThat(response2.overallAverageScore()).isEqualTo(3.45);

            // Test 3: Exactly two decimals (3.50 -> 3.50)
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.50);
            BodyPatternResponse response3 = bodyPatternService.getBodyPatterns(testMemberId);
            assertThat(response3.overallAverageScore()).isEqualTo(3.50);

            // Test 4: Many decimals (4.789123 -> 4.79)
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(4.789123);
            BodyPatternResponse response4 = bodyPatternService.getBodyPatterns(testMemberId);
            assertThat(response4.overallAverageScore()).isEqualTo(4.79);
        }

        @Test
        @DisplayName("모든 태그가 조건을 충족하지 않으면 빈 리스트를 반환한다")
        void getBodyPatterns_WithNoTagsMeetingCriteria_ShouldReturnEmptyLists() {
            // Given
            List<Object[]> mockTagStats = List.<Object[]>of(
                    new Object[]{1L, "LowCount", 4.5, 2},   // High score but low count
                    new Object[]{2L, "MidScore", 3.5, 5},   // Mid score
                    new Object[]{3L, "LowScore", 2.6, 2}    // Low score but low count
            );

            when(foodMemberStatsRepository.findByMemberId(testMemberId))
                    .thenReturn(testFoodStats);
            when(reactionRepository.calculateAverageScoreByMemberId(testMemberId))
                    .thenReturn(3.0);
            when(foodMemberStatsRepository.findTagStatistics(testMemberId))
                    .thenReturn(mockTagStats);

            // When
            BodyPatternResponse response = bodyPatternService.getBodyPatterns(testMemberId);

            // Then
            assertThat(response.goodTags()).isEmpty();
            assertThat(response.badTags()).isEmpty();
        }
    }
}
