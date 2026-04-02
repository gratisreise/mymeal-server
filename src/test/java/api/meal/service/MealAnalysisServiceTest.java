package api.meal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.api.meal.service.MealAnalysisService;
import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.api.recommendation.service.dto.FoodAnalysisResult;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.domain.food.Food;
import com.mymealserver.domain.food.FoodReader;
import com.mymealserver.domain.food.FoodWriter;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisWriter;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class MealAnalysisServiceTest {

  @Mock private AiAnalysisService aiAnalysisService;
  @Mock private MealReader mealReader;
  @Mock private MealWriter mealWriter;
  @Mock private MealAnalysisWriter mealAnalysisWriter;
  @Mock private FoodReader foodReader;
  @Mock private FoodWriter foodWriter;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private MealAnalysisService mealAnalysisService;

  private static final Long MEAL_ID = 1L;
  private static final Long FOOD_ID = 10L;

  // ========================
  // analyzeMealAsync - success
  // ========================

  @Test
  void analyzeMealAsync_success_existingFood() throws Exception {
    // given
    Meal meal = createMeal(MEAL_ID, MealType.LUNCH, AnalysisStatus.PENDING);
    Food existingFood = createFood(FOOD_ID, "비빔밥");
    FoodAnalysisResult analysisResult = FoodAnalysisResult.of("비빔밥", 550.0, 70.0, 25.0, 18.0, 0.92);

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(aiAnalysisService.analyzeFoodImage(nullable(Resource.class), eq(MealType.LUNCH)))
        .willReturn(analysisResult);
    given(foodReader.findByName("비빔밥")).willReturn(Optional.of(existingFood));
    given(objectMapper.writeValueAsString(analysisResult)).willReturn("{\"mealName\":\"비빔밥\"}");

    // when
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.LUNCH, null);

    // then
    assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);

    ArgumentCaptor<MealAnalysis> analysisCaptor = ArgumentCaptor.forClass(MealAnalysis.class);
    then(mealAnalysisWriter).should().save(analysisCaptor.capture());

    MealAnalysis savedAnalysis = analysisCaptor.getValue();
    assertThat(savedAnalysis.getMealId()).isEqualTo(MEAL_ID);
    assertThat(savedAnalysis.getFoodId()).isEqualTo(FOOD_ID);
    assertThat(savedAnalysis.getMealName()).isEqualTo("비빔밥");
    assertThat(savedAnalysis.getCalories()).isEqualTo(550.0);
    assertThat(savedAnalysis.getConfidence()).isEqualTo(0.92);

    then(foodWriter).shouldHaveNoInteractions();
  }

  @Test
  void analyzeMealAsync_success_newFood() throws Exception {
    // given
    Meal meal = createMeal(MEAL_ID, MealType.DINNER, AnalysisStatus.PENDING);
    FoodAnalysisResult analysisResult =
        FoodAnalysisResult.of("새로운음식", 400.0, 50.0, 20.0, 10.0, 0.85);
    Food newFood = createFood(FOOD_ID, "새로운음식");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(aiAnalysisService.analyzeFoodImage(nullable(Resource.class), eq(MealType.DINNER)))
        .willReturn(analysisResult);
    given(foodReader.findByName("새로운음식")).willReturn(Optional.empty());
    given(foodWriter.save(any(Food.class))).willReturn(newFood);
    given(objectMapper.writeValueAsString(analysisResult)).willReturn("{\"mealName\":\"새로운음식\"}");

    // when
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.DINNER, null);

    // then
    assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);

    ArgumentCaptor<Food> foodCaptor = ArgumentCaptor.forClass(Food.class);
    then(foodWriter).should().save(foodCaptor.capture());
    Food savedFood = foodCaptor.getValue();
    assertThat(savedFood.getName()).isEqualTo("새로운음식");
    assertThat(savedFood.getCalories()).isEqualTo(400.0);

    then(mealAnalysisWriter).should().save(any(MealAnalysis.class));
  }

  // ========================
  // analyzeMealAsync - failure
  // ========================

  @Test
  void analyzeMealAsync_failure_aiAnalysisError() {
    // given
    Meal meal = createMeal(MEAL_ID, MealType.LUNCH, AnalysisStatus.PENDING);

    // analyzeMealAsync calls findById, then handleAnalysisFailure calls findById again
    given(mealReader.findById(MEAL_ID)).willReturn(meal).willReturn(meal);
    given(aiAnalysisService.analyzeFoodImage(nullable(Resource.class), eq(MealType.LUNCH)))
        .willThrow(new RuntimeException("AI service unavailable"));

    // when
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.LUNCH, null);

    // then
    assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);

    ArgumentCaptor<MealAnalysis> failureCaptor = ArgumentCaptor.forClass(MealAnalysis.class);
    then(mealAnalysisWriter).should().save(failureCaptor.capture());

    MealAnalysis failureAnalysis = failureCaptor.getValue();
    assertThat(failureAnalysis.getMealName()).isEqualTo("분석 실패");
    assertThat(failureAnalysis.getConfidence()).isEqualTo(0.0);

    then(mealWriter).should().save(meal);
  }

  @Test
  void analyzeMealAsync_failure_handleAnalysisFailureAlsoFails() {
    // given - mealReader.findById throws, so both try and handleAnalysisFailure fail
    given(mealReader.findById(MEAL_ID)).willThrow(new RuntimeException("DB error"));

    // when - should not throw
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.LUNCH, null);

    // then - no exception propagated, error is logged
  }

  // ========================
  // analyzeMealAsync - various meal types
  // ========================

  @Test
  void analyzeMealAsync_success_breakfast() throws Exception {
    // given
    Meal meal = createMeal(MEAL_ID, MealType.BREAKFAST, AnalysisStatus.PENDING);
    FoodAnalysisResult result = FoodAnalysisResult.of("토스트", 300.0, 40.0, 10.0, 12.0, 0.88);
    Food food = createFood(FOOD_ID, "토스트");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(aiAnalysisService.analyzeFoodImage(nullable(Resource.class), eq(MealType.BREAKFAST)))
        .willReturn(result);
    given(foodReader.findByName("토스트")).willReturn(Optional.of(food));
    given(objectMapper.writeValueAsString(result)).willReturn("{}");

    // when
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.BREAKFAST, null);

    // then
    then(aiAnalysisService)
        .should()
        .analyzeFoodImage(nullable(Resource.class), eq(MealType.BREAKFAST));
    assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
  }

  @Test
  void analyzeMealAsync_success_snack() throws Exception {
    // given
    Meal meal = createMeal(MEAL_ID, MealType.SNACK, AnalysisStatus.PENDING);
    FoodAnalysisResult result = FoodAnalysisResult.of("과일", 150.0, 30.0, 2.0, 1.0, 0.75);
    Food food = createFood(FOOD_ID, "과일");

    given(mealReader.findById(MEAL_ID)).willReturn(meal);
    given(aiAnalysisService.analyzeFoodImage(nullable(Resource.class), eq(MealType.SNACK)))
        .willReturn(result);
    given(foodReader.findByName("과일")).willReturn(Optional.of(food));
    given(objectMapper.writeValueAsString(result)).willReturn("{}");

    // when
    mealAnalysisService.analyzeMealAsync(MEAL_ID, MealType.SNACK, null);

    // then
    then(aiAnalysisService).should().analyzeFoodImage(nullable(Resource.class), eq(MealType.SNACK));
  }

  // --- Helper methods ---

  private Meal createMeal(Long id, MealType mealType, AnalysisStatus status) {
    return Meal.builder()
        .id(id)
        .memberId(1L)
        .mealType(mealType)
        .mealTime(LocalDateTime.now())
        .photoUrl("https://s3.example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .analysisStatus(status)
        .build();
  }

  private Food createFood(Long id, String name) {
    return Food.builder()
        .id(id)
        .name(name)
        .calories(500.0)
        .carbohydrates(60.0)
        .protein(25.0)
        .fat(15.0)
        .averageScore(0.0)
        .mealCount(0)
        .build();
  }
}
