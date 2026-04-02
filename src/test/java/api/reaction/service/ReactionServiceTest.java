package api.reaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.mymealserver.api.meal.service.MealLogService;
import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.api.reaction.service.ReactionService;
import com.mymealserver.common.enums.GradeType;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.reaction.ReactionWriter;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

  @Mock private ReactionReader reactionReader;
  @Mock private ReactionWriter reactionWriter;
  @Mock private MealReader mealReader;
  @Mock private MealLogService mealLogService;

  @InjectMocks private ReactionService reactionService;

  private static final Long MEMBER_ID = 1L;
  private static final Long OTHER_MEMBER_ID = 2L;
  private static final Long MEAL_ID = 10L;

  // ========================
  // createReaction
  // ========================

  @Test
  void createReaction_success() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(4, 3, 4, false, false, false, false, "좋아요");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(null);

    Reaction savedReaction = createReaction(1L, MEAL_ID, request);
    given(reactionWriter.save(any(Reaction.class))).willReturn(savedReaction);

    // when
    ReactionResponse response = reactionService.createReaction(MEMBER_ID, MEAL_ID, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.mealId()).isEqualTo(MEAL_ID);
    assertThat(response.digestionLevel()).isEqualTo(4);
    assertThat(response.fullnessLevel()).isEqualTo(3);
    assertThat(response.energyLevel()).isEqualTo(4);
    assertThat(response.memo()).isEqualTo("좋아요");

    then(reactionWriter).should().save(any(Reaction.class));
    then(mealLogService).should().createMealLogAndEmbedAsync(MEAL_ID, 1L);
  }

  @Test
  void createReaction_fail_otherMembersMeal() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(4, 3, 4, false, false, false, false, null);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> reactionService.createReaction(OTHER_MEMBER_ID, MEAL_ID, request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);

    then(reactionWriter).should(never()).save(any());
    then(mealLogService).should(never()).createMealLogAndEmbedAsync(any(), any());
  }

  @Test
  void createReaction_fail_reactionAlreadyExists() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(4, 3, 4, false, false, false, false, null);
    Reaction existingReaction = createReaction(1L, MEAL_ID, request);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(existingReaction);

    // when & then
    assertThatThrownBy(() -> reactionService.createReaction(MEMBER_ID, MEAL_ID, request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.REACTION_ALREADY_EXISTS);

    then(reactionWriter).should(never()).save(any());
    then(mealLogService).should(never()).createMealLogAndEmbedAsync(any(), any());
  }

  // ========================
  // getReactionByMealId
  // ========================

  @Test
  void getReactionByMealId_success() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(5, 5, 5, false, false, false, false, null);
    Reaction reaction = createReaction(1L, MEAL_ID, request);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(reaction);

    // when
    ReactionResponse response = reactionService.getReactionByMealId(MEMBER_ID, MEAL_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.mealId()).isEqualTo(MEAL_ID);
    assertThat(response.digestionLevel()).isEqualTo(5);
    assertThat(response.fullnessLevel()).isEqualTo(5);
    assertThat(response.energyLevel()).isEqualTo(5);
    assertThat(response.overallScore()).isEqualTo(5.0);
    assertThat(response.grade()).isEqualTo(GradeType.GOOD);
  }

  @Test
  void getReactionByMealId_success_noReaction() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(null);

    // when
    ReactionResponse response = reactionService.getReactionByMealId(MEMBER_ID, MEAL_ID);

    // then
    assertThat(response).isNull();
  }

  @Test
  void getReactionByMealId_fail_otherMembersMeal() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    given(mealReader.findById(MEAL_ID)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> reactionService.getReactionByMealId(OTHER_MEMBER_ID, MEAL_ID))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);
  }

  // ========================
  // updateReaction
  // ========================

  @Test
  void updateReaction_success() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest originalRequest =
        createReactionRequest(4, 3, 4, false, false, false, false, "기존 메모");
    Reaction existingReaction = createReaction(1L, MEAL_ID, originalRequest);

    ReactionRequest updateRequest =
        createReactionRequest(2, 2, 1, true, true, true, true, "업데이트 메모");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(existingReaction);
    given(reactionWriter.save(existingReaction)).willReturn(existingReaction);

    // when
    ReactionResponse response = reactionService.updateReaction(MEMBER_ID, MEAL_ID, updateRequest);

    // then
    assertThat(response).isNotNull();
    assertThat(response.digestionLevel()).isEqualTo(2);
    assertThat(response.fullnessLevel()).isEqualTo(2);
    assertThat(response.energyLevel()).isEqualTo(1);
    assertThat(response.hasHeartburn()).isTrue();
    assertThat(response.hasGas()).isTrue();
    assertThat(response.hasBloating()).isTrue();
    assertThat(response.hasHeadache()).isTrue();
    assertThat(response.memo()).isEqualTo("업데이트 메모");

    then(reactionWriter).should().save(existingReaction);
  }

  @Test
  void updateReaction_fail_otherMembersMeal() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(4, 3, 4, false, false, false, false, null);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> reactionService.updateReaction(OTHER_MEMBER_ID, MEAL_ID, request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);

    then(reactionWriter).should(never()).save(any());
  }

  @Test
  void updateReaction_fail_reactionNotFound() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID);
    ReactionRequest request = createReactionRequest(4, 3, 4, false, false, false, false, null);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(null);

    // when & then
    assertThatThrownBy(() -> reactionService.updateReaction(MEMBER_ID, MEAL_ID, request))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.REACTION_NOT_FOUND);

    then(reactionWriter).should(never()).save(any());
  }

  // --- Helper methods ---

  private Meal createMeal(Long mealId, Long memberId) {
    return Meal.builder()
        .id(mealId)
        .memberId(memberId)
        .mealType(MealType.LUNCH)
        .mealTime(LocalDateTime.now())
        .build();
  }

  private Reaction createReaction(Long id, Long mealId, ReactionRequest request) {
    Reaction reaction =
        Reaction.builder()
            .id(id)
            .mealId(mealId)
            .digestionLevel(request.digestionLevel().shortValue())
            .fullnessLevel(request.fullnessLevel().shortValue())
            .energyLevel(request.energyLevel().shortValue())
            .hasHeartburn(request.hasHeartburn())
            .hasGas(request.hasGas())
            .hasBloating(request.hasBloating())
            .hasHeadache(request.hasHeadache())
            .memo(request.memo())
            .build();
    reaction.calculateOverallScore();
    return reaction;
  }

  private ReactionRequest createReactionRequest(
      int digestionLevel,
      int fullnessLevel,
      int energyLevel,
      boolean hasHeartburn,
      boolean hasGas,
      boolean hasBloating,
      boolean hasHeadache,
      String memo) {
    return new ReactionRequest(
        digestionLevel,
        fullnessLevel,
        energyLevel,
        hasHeartburn,
        hasGas,
        hasBloating,
        hasHeadache,
        memo);
  }
}
