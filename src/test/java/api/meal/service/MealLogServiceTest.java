package api.meal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.mymealserver.api.meal.service.MealLogService;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.meallog.MealLog;
import com.mymealserver.domain.meallog.MealLogWriter;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

@ExtendWith(MockitoExtension.class)
class MealLogServiceTest {

  @Mock private MealReader mealReader;
  @Mock private MealAnalysisReader mealAnalysisReader;
  @Mock private ReactionReader reactionReader;
  @Mock private MealLogWriter mealLogWriter;
  @Mock private EmbeddingModel embeddingModel;

  @InjectMocks private MealLogService mealLogService;

  private static final Long MEAL_ID = 1L;
  private static final Long MEMBER_ID = 1L;
  private static final Long REACTION_ID = 10L;

  // ========================
  // createMealLogAndEmbedAsync - success
  // ========================

  @Test
  void createMealLogAndEmbedAsync_success() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID, MealType.LUNCH);
    MealAnalysis analysis = createMealAnalysis(100L, MEAL_ID, "비빔밥");
    Reaction reaction = createReaction(REACTION_ID, MEAL_ID);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(MEAL_ID)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(reaction);
    given(embeddingModel.embed(any(String.class))).willReturn(new float[] {0.1f, 0.2f, 0.3f});

    // when
    mealLogService.createMealLogAndEmbedAsync(MEAL_ID, REACTION_ID);

    // then
    then(embeddingModel).should().embed(any(String.class));
    then(mealLogWriter).should().saveWithEmbed(any(String.class));

    ArgumentCaptor<MealLog> logCaptor = ArgumentCaptor.forClass(MealLog.class);
    then(mealLogWriter).should().save(logCaptor.capture());

    MealLog savedLog = logCaptor.getValue();
    assertThat(savedLog.getMealId()).isEqualTo(MEAL_ID);
    assertThat(savedLog.getMemberId()).isEqualTo(MEMBER_ID);
    assertThat(savedLog.getMealSummary()).contains("비빔밥");
    assertThat(savedLog.getMealSummary()).contains("점심");
    assertThat(savedLog.getReactionSummary()).contains("소화 상태");
    assertThat(savedLog.getCombinedSummary()).contains("비빔밥");
    assertThat(savedLog.getCombinedSummary()).contains("소화 상태");
  }

  @Test
  void createMealLogAndEmbedAsync_success_containsNutritionalInfo() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID, MealType.BREAKFAST);
    MealAnalysis analysis = createMealAnalysis(100L, MEAL_ID, "토스트");
    Reaction reaction = createReaction(REACTION_ID, MEAL_ID);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(MEAL_ID)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(reaction);
    given(embeddingModel.embed(any(String.class))).willReturn(new float[] {0.5f});

    // when
    mealLogService.createMealLogAndEmbedAsync(MEAL_ID, REACTION_ID);

    // then
    ArgumentCaptor<MealLog> logCaptor = ArgumentCaptor.forClass(MealLog.class);
    then(mealLogWriter).should().save(logCaptor.capture());

    MealLog savedLog = logCaptor.getValue();
    assertThat(savedLog.getMealSummary()).contains("500kcal");
    assertThat(savedLog.getMealSummary()).contains("탄수화물");
    assertThat(savedLog.getMealSummary()).contains("단백질");
  }

  @Test
  void createMealLogAndEmbedAsync_success_reactionWithSymptoms() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID, MealType.DINNER);
    MealAnalysis analysis = createMealAnalysis(100L, MEAL_ID, "피자");
    Reaction reaction =
        Reaction.builder()
            .id(REACTION_ID)
            .mealId(MEAL_ID)
            .digestionLevel((short) 2)
            .fullnessLevel((short) 5)
            .energyLevel((short) 3)
            .hasHeartburn(true)
            .hasGas(true)
            .hasBloating(false)
            .hasHeadache(false)
            .memo("너무 매워요")
            .build();

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(MEAL_ID)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(reaction);
    given(embeddingModel.embed(any(String.class))).willReturn(new float[] {0.1f});

    // when
    mealLogService.createMealLogAndEmbedAsync(MEAL_ID, REACTION_ID);

    // then
    ArgumentCaptor<MealLog> logCaptor = ArgumentCaptor.forClass(MealLog.class);
    then(mealLogWriter).should().save(logCaptor.capture());

    String reactionSummary = logCaptor.getValue().getReactionSummary();
    assertThat(reactionSummary).contains("속쓰림");
    assertThat(reactionSummary).contains("가스");
    assertThat(reactionSummary).contains("메모: 너무 매워요");
  }

  // ========================
  // createMealLogAndEmbedAsync - failure
  // ========================

  @Test
  void createMealLogAndEmbedAsync_fail_analysisNotFound() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID, MealType.LUNCH);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(MEAL_ID)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> mealLogService.createMealLogAndEmbedAsync(MEAL_ID, REACTION_ID))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_ANALYSIS_NOT_FOUND);
  }

  @Test
  void createMealLogAndEmbedAsync_fail_reactionNotFound() {
    // given
    Meal meal = createMeal(MEAL_ID, MEMBER_ID, MealType.LUNCH);
    MealAnalysis analysis = createMealAnalysis(100L, MEAL_ID, "비빔밥");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(MEAL_ID)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(MEAL_ID)).willReturn(null);

    // when & then
    assertThatThrownBy(() -> mealLogService.createMealLogAndEmbedAsync(MEAL_ID, REACTION_ID))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.REACTION_NOT_FOUND);
  }

  // --- Helper methods ---

  private Meal createMeal(Long id, Long memberId, MealType mealType) {
    return Meal.builder()
        .id(id)
        .memberId(memberId)
        .mealType(mealType)
        .mealTime(LocalDateTime.now())
        .photoUrl("https://s3.example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .build();
  }

  private MealAnalysis createMealAnalysis(Long id, Long mealId, String mealName) {
    return MealAnalysis.builder()
        .id(id)
        .mealId(mealId)
        .mealName(mealName)
        .calories(500.0)
        .carbohydrates(60.0)
        .protein(25.0)
        .fat(15.0)
        .confidence(0.9)
        .build();
  }

  private Reaction createReaction(Long id, Long mealId) {
    return Reaction.builder()
        .id(id)
        .mealId(mealId)
        .digestionLevel((short) 4)
        .fullnessLevel((short) 3)
        .energyLevel((short) 4)
        .hasHeartburn(false)
        .hasGas(false)
        .hasBloating(false)
        .hasHeadache(false)
        .build();
  }
}
