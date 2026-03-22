package com.mymealserver.api.meal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.api.recommendation.service.FoodAnalysisResult;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.FoodFixture;
import com.mymealserver.common.test.fixtures.MealFixture;
import com.mymealserver.domain.food.Food;
import com.mymealserver.domain.food.FoodReader;
import com.mymealserver.domain.food.FoodWriter;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisWriter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealAnalysisService 단위 테스트")
class MealAnalysisServiceTest {

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private MealReader mealReader;

    @Mock
    private MealWriter mealWriter;

    @Mock
    private MealAnalysisWriter mealAnalysisWriter;

    @Mock
    private FoodReader foodReader;

    @Mock
    private FoodWriter foodWriter;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MealAnalysisService mealAnalysisService;

    private final Long testMealId = 1L;
    private final Long testMemberId = 1L;
    private final MealType testMealType = MealType.LUNCH;

    private Resource testImageResource;
    private FoodAnalysisResult testAnalysisResult;

    @BeforeEach
    void setUp() throws Exception {
        // Create test resource
        byte[] imageBytes = "test-image-content".getBytes();
        testImageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "test-meal.jpg";
            }
        };

        // Create test analysis result
        testAnalysisResult = new FoodAnalysisResult(
                "김치찌개",
                450.0,
                65.0,
                25.0,
                12.0,
                0.92
        );

        // ObjectMapper default behavior
        lenient().when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"analysis\":\"result\"}");

        // MealWriter default behavior
        lenient().when(mealWriter.save(any(Meal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // FoodWriter default behavior
        lenient().when(foodWriter.save(any(Food.class)))
                .thenAnswer(invocation -> {
                    Food food = invocation.getArgument(0);
                    if (food.getId() == null) {
                        ReflectionTestUtils.setField(food, "id", 999L);
                    }
                    return food;
                });
    }

    @Nested
    @DisplayName("AI 분석 비동기 처리 (analyzeMealAsync)")
    class AnalyzeMealAsyncTests {

        @Test
        @DisplayName("정상적으로 AI 분석을 완료하고 상태를 COMPLETED로 변경한다")
        void analyzeMealAsync_WithValidData_ShouldCompleteSuccessfully() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(testImageResource), eq(testMealType)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class))).thenReturn(MealAnalysis.builder().build());

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
            verify(aiAnalysisService).analyzeFoodImage(eq(testImageResource), eq(testMealType));
            verify(foodReader).findByName("김치찌개");
            verify(mealAnalysisWriter).save(any(MealAnalysis.class));
            verify(mealWriter).save(meal);
            verify(foodWriter, never()).save(any(Food.class)); // Should not create new food
        }

        @Test
        @DisplayName("Food가 없으면 자동으로 새로운 Food를 생성한다")
        void analyzeMealAsync_WithNewFood_ShouldCreateFoodAutomatically() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            String newFoodName = "새로운음식";
            FoodAnalysisResult newFoodResult = new FoodAnalysisResult(
                    newFoodName,
                    500.0,
                    60.0,
                    20.0,
                    15.0,
                    0.85
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(testImageResource), eq(testMealType)))
                    .thenReturn(newFoodResult);
            when(foodReader.findByName(newFoodName)).thenReturn(Optional.empty());
            when(mealAnalysisWriter.save(any(MealAnalysis.class))).thenReturn(MealAnalysis.builder().build());

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(foodWriter).save(any(Food.class));
            verify(mealAnalysisWriter).save(argThat(analysis ->
                    analysis.getMealName().equals(newFoodName) &&
                            analysis.getFoodId() != null // Food ID should be linked
            ));
        }

        @Test
        @DisplayName("FoodAnalysisResult의 모든 영양성분이 MealAnalysis에 정확히 저장된다")
        void analyzeMealAsync_ShouldStoreAllNutritionalInfoCorrectly() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(testImageResource), eq(testMealType)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(mealAnalysisWriter).save(argThat(analysis ->
                    analysis.getMealId().equals(testMealId) &&
                            analysis.getFoodId().equals(existingFood.getId()) &&
                            analysis.getMealName().equals("김치찌개") &&
                            analysis.getCalories() == 450.0 &&
                            analysis.getCarbohydrates() == 65.0 &&
                            analysis.getProtein() == 25.0 &&
                            analysis.getFat() == 12.0 &&
                            analysis.getConfidence() == 0.92
            ));
        }

        @Test
        @DisplayName("AI 분석 결과의 rawResponse가 JSON으로 저장된다")
        void analyzeMealAsync_ShouldStoreRawResponseAsJson() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();
            String expectedJson = "{\"mealName\":\"김치찌개\",\"calories\":450.0}";

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(testImageResource), eq(testMealType)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(objectMapper.writeValueAsString(testAnalysisResult)).thenReturn(expectedJson);
            when(mealAnalysisWriter.save(any(MealAnalysis.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(mealAnalysisWriter).save(argThat(analysis ->
                    analysis.getRawResponse().equals(expectedJson)
            ));
            verify(objectMapper).writeValueAsString(testAnalysisResult);
        }

        @Test
        @DisplayName("다양한 MealType으로 분석이 성공한다")
        void analyzeMealAsync_WithDifferentMealTypes_ShouldSucceed() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultDinner();
            Food existingFood = FoodFixture.createDoenjangStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(testImageResource), eq(MealType.DINNER)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class))).thenReturn(MealAnalysis.builder().build());

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, MealType.DINNER, testImageResource);

            // Then
            verify(aiAnalysisService).analyzeFoodImage(eq(testImageResource), eq(MealType.DINNER));
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("AI 분석 실패 처리 (handleAnalysisFailure)")
    class AnalysisFailureTests {

        @Test
        @DisplayName("AI 분석 실패 시 상태를 FAILED로 변경한다")
        void analyzeMealAsync_WhenAiServiceThrowsException_ShouldMarkAsFailed() {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), any(MealType.class)))
                    .thenThrow(new RuntimeException("AI service unavailable"));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
            verify(mealWriter).save(meal);
        }

        @Test
        @DisplayName("AI 분석 실패 시 실패 기록을 저장한다")
        void analyzeMealAsync_WhenAiServiceThrowsException_ShouldStoreFailureRecord() {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), any(MealType.class)))
                    .thenThrow(new RuntimeException("Network timeout"));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(mealAnalysisWriter).save(argThat(analysis ->
                    analysis.getMealId().equals(testMealId) &&
                            analysis.getMealName().equals("분석 실패") &&
                            analysis.getConfidence() == 0.0 &&
                            analysis.getRawResponse().contains("Network timeout")
            ));
        }

        @Test
        @DisplayName("Meal 조회 실패 시 적절히 처리한다")
        void analyzeMealAsync_WhenMealNotFound_ShouldHandleGracefully() {
            // Given
            when(mealReader.findById(testMealId))
                    .thenThrow(new RuntimeException(ErrorCode.MEAL_NOT_FOUND.getMessage()));

            // When - Should not throw exception, just log error
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then - Verify no further processing attempted
            verify(aiAnalysisService, never()).analyzeFoodImage(any(), any());
            verify(mealWriter, never()).save(any());
        }

        @Test
        @DisplayName("FoodReader 실패 시에도 Meal 상태를 FAILED로 변경한다")
        void analyzeMealAsync_WhenFoodReaderFails_ShouldMarkAsFailed() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), any(MealType.class)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName(anyString()))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
        }

        @Test
        @DisplayName("MealAnalysis 저장 실패 시에도 Meal 상태를 FAILED로 변경한다")
        void analyzeMealAsync_WhenSavingAnalysisFails_ShouldMarkAsFailed() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), any(MealType.class)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName(anyString())).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class)))
                    .thenThrow(new RuntimeException("Save failed"));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("Resource 변환 및 스레드 안전성")
    class ResourceHandlingTests {

        @Test
        @DisplayName("ByteArrayResource가 비동기 실행에서 정상 동작한다")
        void analyzeMealAsync_WithByteArrayResource_ShouldWorkCorrectly() throws Exception {
            // Given
            byte[] largeImageBytes = new byte[1024 * 1024]; // 1MB image
            ByteArrayResource largeResource = new ByteArrayResource(largeImageBytes) {
                @Override
                public String getFilename() {
                    return "large-meal.jpg";
                }
            };

            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(eq(largeResource), eq(testMealType)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class))).thenReturn(MealAnalysis.builder().build());

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, largeResource);

            // Then
            verify(aiAnalysisService).analyzeFoodImage(eq(largeResource), eq(testMealType));
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        }

        @Test
        @DisplayName("Resource의 getFilename()이 null이어도 동작한다")
        void analyzeMealAsync_WithNullFilename_ShouldStillWork() throws Exception {
            // Given
            ByteArrayResource resourceWithoutFilename = new ByteArrayResource("test".getBytes());

            Meal meal = MealFixture.createDefaultLunch();
            Food existingFood = FoodFixture.createKimchiStew();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), eq(testMealType)))
                    .thenReturn(testAnalysisResult);
            when(foodReader.findByName("김치찌개")).thenReturn(Optional.of(existingFood));
            when(mealAnalysisWriter.save(any(MealAnalysis.class))).thenReturn(MealAnalysis.builder().build());

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, resourceWithoutFilename);

            // Then
            verify(aiAnalysisService).analyzeFoodImage(any(Resource.class), eq(testMealType));
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Food 자동 생성 상세 검증")
    class FoodAutoCreationTests {

        @Test
        @DisplayName("새 Food 생성 시 AI 분석 결과의 모든 영양성분이 포함된다")
        void createNewFood_ShouldIncludeAllNutritionalInfo() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            String newFoodName = "돈가스";
            FoodAnalysisResult newFoodResult = new FoodAnalysisResult(
                    newFoodName,
                    650.0,
                    45.0,
                    22.0,
                    28.0,
                    0.89
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), eq(testMealType)))
                    .thenReturn(newFoodResult);
            when(foodReader.findByName(newFoodName)).thenReturn(Optional.empty());
            when(mealAnalysisWriter.save(any(MealAnalysis.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(foodWriter).save(argThat(food ->
                    food.getName().equals(newFoodName) &&
                            food.getCalories() == 650.0 &&
                            food.getCarbohydrates() == 45.0 &&
                            food.getProtein() == 22.0 &&
                            food.getFat() == 28.0 &&
                            food.getAverageScore() == 0.0 &&
                            food.getMealCount() == 0
            ));
        }

        @Test
        @DisplayName("새 Food 생성 후 MealAnalysis에 foodId가 연결된다")
        void createNewFood_ShouldLinkFoodIdToMealAnalysis() throws Exception {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            String newFoodName = "새로운음식";
            FoodAnalysisResult newFoodResult = new FoodAnalysisResult(
                    newFoodName,
                    500.0,
                    60.0,
                    20.0,
                    15.0,
                    0.85
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(aiAnalysisService.analyzeFoodImage(any(Resource.class), eq(testMealType)))
                    .thenReturn(newFoodResult);
            when(foodReader.findByName(newFoodName)).thenReturn(Optional.empty());
            when(mealAnalysisWriter.save(any(MealAnalysis.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            mealAnalysisService.analyzeMealAsync(testMealId, testMealType, testImageResource);

            // Then
            verify(mealAnalysisWriter).save(argThat(analysis ->
                    analysis.getMealId().equals(testMealId) &&
                            analysis.getFoodId() != null &&
                            analysis.getFoodId().equals(999L) // Auto-generated ID
            ));
        }
    }
}
