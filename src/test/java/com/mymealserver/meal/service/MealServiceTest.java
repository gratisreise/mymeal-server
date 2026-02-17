package com.mymealserver.meal.service;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.MealAnalysisFixture;
import com.mymealserver.common.test.fixtures.MealFixture;
import com.mymealserver.common.test.fixtures.MultipartFileFixture;
import com.mymealserver.common.test.fixtures.ReactionFixture;
import com.mymealserver.domain.meal.MealAnalysisReader;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.MealAnalysis;
import com.mymealserver.entity.Reaction;
import com.mymealserver.entity.enums.AnalysisStatus;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.meal.dto.request.MealCreateRequest;
import com.mymealserver.meal.dto.request.MealRetakePhotoRequest;
import com.mymealserver.meal.dto.response.AIAnalysisResponse;
import com.mymealserver.meal.dto.response.MealDetailResponse;
import com.mymealserver.meal.dto.response.MealResponse;
import com.mymealserver.reaction.domain.ReactionReader;
import com.mymealserver.reaction.dto.response.ReactionResponse;
import com.mymealserver.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MealService 단위 테스트")
class MealServiceTest {

    @Mock
    private MealReader mealReader;

    @Mock
    private MealWriter mealWriter;

    @Mock
    private ReactionReader reactionReader;

    @Mock
    private MealAnalysisReader mealAnalysisReader;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MealAnalysisService mealAnalysisService;

    @InjectMocks
    private MealService mealService;

    private final Long testMemberId = 1L;
    private final Long testMealId = 1L;

    @BeforeEach
    void setUp() {
        // FileStorageService 기본 mock 설정 (lenient()로 불필요한 stubbing 경고 방지)
        lenient().when(fileStorageService.uploadMealPhoto(any(MultipartFile.class), anyLong()))
                .thenReturn("https://s3.amazonaws.com/meals/1/2025/02/test.jpg");
        lenient().when(fileStorageService.extractPhotoKey(anyString()))
                .thenReturn("meals/1/2025/02/test.jpg");
        lenient().doNothing().when(fileStorageService).deletePhoto(anyString());

        // MealWriter 기본 mock 설정
        lenient().when(mealWriter.save(any(Meal.class)))
                .thenAnswer(invocation -> {
                    Meal meal = invocation.getArgument(0);
                    Meal savedMeal = Meal.builder()
                            .id(1L)
                            .memberId(meal.getMemberId())
                            .mealType(meal.getMealType())
                            .mealTime(meal.getMealTime())
                            .photoUrl(meal.getPhotoUrl())
                            .photoKey(meal.getPhotoKey())
                            .analysisStatus(meal.getAnalysisStatus())
                            .memo(meal.getMemo())
                            .build();
                    return savedMeal;
                });
        lenient().doNothing().when(mealWriter).delete(any(Meal.class));

        // MealAnalysisService 기본 mock 설정
        lenient().doAnswer(invocation -> null)
                .when(mealAnalysisService).analyzeMealAsync(anyLong(), any(MultipartFile.class));
    }

    @Nested
    @DisplayName("식사 생성 (createMeal)")
    class CreateMealTests {

        @Test
        @DisplayName("유효한 데이터로 식사 생성에 성공한다")
        void createMeal_WithValidData_ShouldReturnMealResponse() {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    MealType.LUNCH,
                    LocalDateTime.of(2025, 2, 15, 12, 0),
                    "점심 식사"
            );

            // When
            MealResponse response = mealService.createMeal(testMemberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.photoUrl()).contains("s3.amazonaws.com");
            assertThat(response.hasReaction()).isFalse();

            verify(fileStorageService).uploadMealPhoto(any(), eq(testMemberId));
            verify(fileStorageService).extractPhotoKey(anyString());
            verify(mealWriter).save(any(Meal.class));
            verify(mealAnalysisService).analyzeMealAsync(eq(1L), any(MultipartFile.class));
        }

        @Test
        @DisplayName("mealTime이 null이면 현재 시간을 사용한다")
        void createMeal_WithNullMealTime_ShouldUseCurrentTime() {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    MealType.DINNER,
                    null,
                    "저녁 식사"
            );

            // When
            MealResponse response = mealService.createMeal(testMemberId, request);

            // Then
            assertThat(response).isNotNull();
            verify(mealWriter).save(any(Meal.class));
        }

        @ParameterizedTest
        @EnumSource(MealType.class)
        @DisplayName("모든 MealType으로 식사 생성에 성공한다")
        void createMeal_WithAllMealTypes_ShouldSucceed(MealType mealType) {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    mealType,
                    LocalDateTime.now(),
                    "테스트 식사"
            );

            // When
            MealResponse response = mealService.createMeal(testMemberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.mealType()).isEqualTo(mealType);
        }

        @Test
        @DisplayName("memo가 null이어도 식사 생성에 성공한다")
        void createMeal_WithNullMemo_ShouldSucceed() {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    MealType.SNACK,
                    LocalDateTime.now(),
                    null
            );

            // When
            MealResponse response = mealService.createMeal(testMemberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.memo()).isNull();
        }

        @Test
        @DisplayName("memo가 빈 문자열이어도 식사 생성에 성공한다")
        void createMeal_WithEmptyMemo_ShouldSucceed() {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    MealType.BREAKFAST,
                    LocalDateTime.now(),
                    ""
            );

            // When
            MealResponse response = mealService.createMeal(testMemberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.memo()).isEmpty();
        }

        @Test
        @DisplayName("AI 분석이 비동기로 호출되는지 확인한다")
        void createMeal_ShouldTriggerAIAnalysisAsync() {
            // Given
            MealCreateRequest request = new MealCreateRequest(
                    MultipartFileFixture.createValidJpegPhoto(),
                    MealType.LUNCH,
                    LocalDateTime.now(),
                    "AI 분석 테스트"
            );

            // When
            mealService.createMeal(testMemberId, request);

            // Then
            verify(mealAnalysisService).analyzeMealAsync(anyLong(), any(MultipartFile.class));
        }
    }

    @Nested
    @DisplayName("식사 목록 조회 (getMeals)")
    class GetMealsTests {

        @Test
        @DisplayName("필터 없이 전체 식사 목록을 조회한다")
        void getMeals_WithoutFilters_ShouldReturnAllMeals() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Meal> meals = List.of(
                    MealFixture.createDefaultBreakfast(),
                    MealFixture.createDefaultLunch(),
                    MealFixture.createDefaultDinner()
            );
            Page<Meal> mealPage = new PageImpl<>(meals);

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(),
                    isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(anyLong())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, null,
                    null, null, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(3);
            assertThat(response.getContent()).allMatch(m -> !m.hasReaction());
        }

        @Test
        @DisplayName("날짜 범위로 필터링하여 식사 목록을 조회한다")
        void getMeals_WithDateRange_ShouldReturnFilteredMeals() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            LocalDate startDate = LocalDate.of(2025, 2, 1);
            LocalDate endDate = LocalDate.of(2025, 2, 15);
            List<Meal> meals = List.of(MealFixture.createDefaultLunch());
            Page<Meal> mealPage = new PageImpl<>(meals);

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), eq(endDate),
                    isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(anyLong())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, startDate,
                    endDate, null, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("MealType으로 필터링하여 식사 목록을 조회한다")
        void getMeals_WithMealType_ShouldReturnFilteredMeals() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            List<Meal> meals = List.of(MealFixture.createDefaultLunch());
            Page<Meal> mealPage = new PageImpl<>(meals);

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(),
                    eq(MealType.LUNCH), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(anyLong())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, null,
                    null, MealType.LUNCH, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getContent().get(0).mealType()).isEqualTo(MealType.LUNCH);
        }

        @Test
        @DisplayName("날짜와 MealType으로 복합 필터링하여 식사 목록을 조회한다")
        void getMeals_WithDateRangeAndMealType_ShouldReturnFilteredMeals() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            LocalDate startDate = LocalDate.of(2025, 2, 1);
            LocalDate endDate = LocalDate.of(2025, 2, 28);
            List<Meal> meals = List.of(MealFixture.createDefaultDinner());
            Page<Meal> mealPage = new PageImpl<>(meals);

            when(mealReader.findByMemberId(eq(testMemberId), eq(startDate), eq(endDate),
                    eq(MealType.DINNER), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(anyLong())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, startDate,
                    endDate, MealType.DINNER, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("해당 기간에 식사가 없으면 빈 페이지를 반환한다")
        void getMeals_WithNoMealsInPeriod_ShouldReturnEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Meal> emptyPage = Page.empty(pageable);

            when(mealReader.findByMemberId(eq(testMemberId), any(), any(),
                    any(), eq(pageable)))
                    .thenReturn(emptyPage);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                    null, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("반응 데이터 존재 여부를 정확히 확인한다")
        void getMeals_ShouldCheckReactionExistence() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Meal meal1 = MealFixture.createDefaultBreakfast();
            Meal meal2 = MealFixture.createDefaultLunch();
            List<Meal> meals = List.of(meal1, meal2);
            Page<Meal> mealPage = new PageImpl<>(meals);

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(),
                    isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(meal1.getId())).thenReturn(true);
            when(reactionReader.existsByMealId(meal2.getId())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, null,
                    null, null, pageable);

            // Then
            assertThat(response.getContent().get(0).hasReaction()).isTrue();
            assertThat(response.getContent().get(1).hasReaction()).isFalse();
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void getMeals_WithPagination_ShouldReturnCorrectPage() {
            // Given
            Pageable pageable = PageRequest.of(1, 2); // 2페이지, 페이지당 2개
            List<Meal> meals = List.of(MealFixture.createDefaultDinner());
            Page<Meal> mealPage = new PageImpl<>(meals, pageable, 5); // 총 5개 항목

            when(mealReader.findByMemberId(eq(testMemberId), isNull(), isNull(),
                    isNull(), eq(pageable)))
                    .thenReturn(mealPage);
            when(reactionReader.existsByMealId(anyLong())).thenReturn(false);

            // When
            Page<MealResponse> response = mealService.getMeals(testMemberId, null,
                    null, null, pageable);

            // Then
            assertThat(response.getNumber()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(2);
            assertThat(response.getTotalElements()).isEqualTo(5);
            assertThat(response.getTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("식사 상세 조회 (getMealDetail)")
    class GetMealDetailTests {

        @Test
        @DisplayName("AI 분석 완료된 식사 상세를 조회한다 (반응 있음)")
        void getMealDetail_WithAnalysisAndReaction_ShouldReturnCompleteDetail() {
            // Given
            Meal meal = MealFixture.createMealWithCompletedAnalysis();
            MealAnalysis analysis = MealAnalysisFixture.createDefaultAnalysis();
            Reaction reaction = ReactionFixture.createDefaultReaction();

            when(mealReader.findById(meal.getId())).thenReturn(meal);
            when(mealAnalysisReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.of(analysis));
            when(reactionReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.of(reaction));

            // When
            MealDetailResponse response = mealService.getMealDetail(testMemberId, meal.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(meal.getId());
            assertThat(response.aiAnalysis()).isNotNull();
            assertThat(response.aiAnalysis().mealName()).isEqualTo("김치찌개");
            assertThat(response.reaction()).isNotNull();
            assertThat(response.hasReaction()).isTrue();
        }

        @Test
        @DisplayName("AI 분석 완료된 식사 상세를 조회한다 (반응 없음)")
        void getMealDetail_WithAnalysisNoReaction_ShouldReturnDetailWithoutReaction() {
            // Given
            Meal meal = MealFixture.createMealWithCompletedAnalysis();
            MealAnalysis analysis = MealAnalysisFixture.createDefaultAnalysis();

            when(mealReader.findById(meal.getId())).thenReturn(meal);
            when(mealAnalysisReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.of(analysis));
            when(reactionReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.empty());

            // When
            MealDetailResponse response = mealService.getMealDetail(testMemberId, meal.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.aiAnalysis()).isNotNull();
            assertThat(response.reaction()).isNull();
            assertThat(response.hasReaction()).isFalse();
        }

        @Test
        @DisplayName("AI 분석 대기 중인 식사 상세를 조회한다 (반응 있음)")
        void getMealDetail_WithPendingAnalysisAndReaction_ShouldReturnPartialDetail() {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            Reaction reaction = ReactionFixture.createDefaultReaction();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(mealAnalysisReader.findByMealId(testMealId))
                    .thenReturn(Optional.empty());
            when(reactionReader.findByMealId(testMealId))
                    .thenReturn(Optional.of(reaction));

            // When
            MealDetailResponse response = mealService.getMealDetail(testMemberId, testMealId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.aiAnalysis()).isNull();
            assertThat(response.reaction()).isNotNull();
            assertThat(response.hasReaction()).isTrue();
        }

        @Test
        @DisplayName("AI 분석 대기 중인 식사 상세를 조회한다 (반응 없음)")
        void getMealDetail_WithPendingAnalysisNoReaction_ShouldReturnMinimalDetail() {
            // Given
            Meal meal = MealFixture.createDefaultLunch();

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(mealAnalysisReader.findByMealId(testMealId))
                    .thenReturn(Optional.empty());
            when(reactionReader.findByMealId(testMealId))
                    .thenReturn(Optional.empty());

            // When
            MealDetailResponse response = mealService.getMealDetail(testMemberId, testMealId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.aiAnalysis()).isNull();
            assertThat(response.reaction()).isNull();
            assertThat(response.hasReaction()).isFalse();
        }

        @Test
        @DisplayName("다른 사용자의 식사 상세 조회 시 예외가 발생한다")
        void getMealDetail_WithDifferentMember_ShouldThrowException() {
            // Given
            Long differentMemberId = 999L;
            Meal meal = MealFixture.createDefaultLunch();

            when(mealReader.findById(testMealId)).thenReturn(meal);

            // When & Then
            assertThatThrownBy(() -> mealService.getMealDetail(differentMemberId, testMealId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_FORBIDDEN));
        }

        @Test
        @DisplayName("존재하지 않는 식사 상세 조회 시 예외가 발생한다")
        void getMealDetail_WithNonExistentMeal_ShouldThrowException() {
            // Given
            when(mealReader.findById(testMealId))
                    .thenThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> mealService.getMealDetail(testMemberId, testMealId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_NOT_FOUND));
        }

        @Test
        @DisplayName("AI 분석 상태가 COMPLETED지만 분석 데이터가 없는 경우")
        void getMealDetail_WithCompletedStatusButNoAnalysisData_ShouldHandleGracefully() {
            // Given
            Meal meal = MealFixture.createMealWithCompletedAnalysis();

            when(mealReader.findById(meal.getId())).thenReturn(meal);
            when(mealAnalysisReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.empty());
            when(reactionReader.findByMealId(meal.getId()))
                    .thenReturn(Optional.empty());

            // When
            MealDetailResponse response = mealService.getMealDetail(testMemberId, meal.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.aiAnalysis()).isNull();
        }
    }

    @Nested
    @DisplayName("식사 삭제 (deleteMeal)")
    class DeleteMealTests {

        @Test
        @DisplayName("식사를 Soft Delete로 삭제한다")
        void deleteMeal_WithOwnership_ShouldSoftDelete() {
            // Given
            Meal meal = MealFixture.createDefaultBreakfast();
            when(mealReader.findById(testMealId)).thenReturn(meal);

            // When
            mealService.deleteMeal(testMemberId, testMealId);

            // Then
            verify(mealWriter).delete(meal);
        }

        @Test
        @DisplayName("다른 사용자의 식사 삭제 시 예외가 발생한다")
        void deleteMeal_WithDifferentMember_ShouldThrowException() {
            // Given
            Long differentMemberId = 999L;
            Meal meal = MealFixture.createDefaultBreakfast();

            when(mealReader.findById(testMealId)).thenReturn(meal);

            // When & Then
            assertThatThrownBy(() -> mealService.deleteMeal(differentMemberId, testMealId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_FORBIDDEN));

            verify(mealWriter, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 식사 삭제 시 예외가 발생한다")
        void deleteMeal_WithNonExistentMeal_ShouldThrowException() {
            // Given
            when(mealReader.findById(testMealId))
                    .thenThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> mealService.deleteMeal(testMemberId, testMealId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_NOT_FOUND));

            verify(mealWriter, never()).delete(any());
        }

        @Test
        @DisplayName("이미 삭제된 식사 삭제도 처리한다")
        void deleteMeal_WithAlreadyDeletedMeal_ShouldHandle() {
            // Given
            Meal meal = MealFixture.createDefaultBreakfast();
            meal.softDelete(); // 이미 삭제됨
            when(mealReader.findById(testMealId)).thenReturn(meal);

            // When
            mealService.deleteMeal(testMemberId, testMealId);

            // Then
            verify(mealWriter).delete(meal);
        }
    }

    @Nested
    @DisplayName("사진 재촬영 (retakePhoto)")
    class RetakePhotoTests {

        @Test
        @DisplayName("식사 사진을 성공적으로 재촬영한다")
        void retakePhoto_WithValidData_ShouldReplacePhotoAndRetriggerAnalysis() {
            // Given
            Meal meal = MealFixture.createDefaultBreakfast();
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(fileStorageService.uploadMealPhoto(any(), eq(testMemberId)))
                    .thenReturn("https://s3.amazonaws.com/meals/1/2025/02/new-uuid.jpg");
            when(fileStorageService.extractPhotoKey(anyString()))
                    .thenReturn("meals/1/2025/02/new-uuid.jpg");
            when(reactionReader.existsByMealId(testMealId)).thenReturn(false);

            // When
            MealResponse response = mealService.retakePhoto(testMemberId, testMealId, request);

            // Then
            assertThat(response.photoUrl()).contains("new-uuid");
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.PENDING); // Verify state change

            verify(fileStorageService).uploadMealPhoto(any(), eq(testMemberId));
            verify(fileStorageService).deletePhoto(anyString());
            verify(mealWriter).save(meal);
            verify(mealAnalysisService).analyzeMealAsync(eq(testMealId), any(MultipartFile.class));
        }

        @Test
        @DisplayName("반응 데이터가 있어도 보존된다")
        void retakePhoto_WithExistingReaction_ShouldPreserveReaction() {
            // Given
            Meal meal = MealFixture.createDefaultLunch();
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(meal.getId())).thenReturn(meal);
            // These methods are called by retakePhoto, so we need to trigger the lenient stubs
            lenient().when(fileStorageService.uploadMealPhoto(any(), eq(testMemberId)))
                    .thenReturn("https://s3.amazonaws.com/meals/1/2025/02/new.jpg");
            lenient().when(fileStorageService.extractPhotoKey(anyString()))
                    .thenReturn("meals/1/2025/02/new.jpg");
            when(reactionReader.existsByMealId(meal.getId())).thenReturn(true);

            // When
            MealResponse response = mealService.retakePhoto(testMemberId, meal.getId(), request);

            // Then
            assertThat(response.hasReaction()).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 식사 사진 재촬영 시 예외가 발생한다")
        void retakePhoto_WithDifferentMember_ShouldThrowException() {
            // Given
            Long differentMemberId = 999L;
            Meal meal = MealFixture.createDefaultBreakfast();
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);

            // When & Then
            assertThatThrownBy(() -> mealService.retakePhoto(differentMemberId, testMealId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_FORBIDDEN));

            verify(fileStorageService, never()).uploadMealPhoto(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 식사의 사진 재촬영 시 예외가 발생한다")
        void retakePhoto_WithNonExistentMeal_ShouldThrowException() {
            // Given
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(testMealId))
                    .thenThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> mealService.retakePhoto(testMemberId, testMealId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEAL_NOT_FOUND));

            verify(fileStorageService, never()).uploadMealPhoto(any(), any());
        }

        @Test
        @DisplayName("AI 분석 상태가 PENDING으로 변경된다")
        void retakePhoto_ShouldUpdateAnalysisStatusToPending() {
            // Given
            Meal meal = MealFixture.createMealWithCompletedAnalysis(); // COMPLETED 상태
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(meal.getId())).thenReturn(meal);
            when(reactionReader.existsByMealId(meal.getId())).thenReturn(false);

            // When
            mealService.retakePhoto(testMemberId, meal.getId(), request);

            // Then
            // Verify the state was updated
            assertThat(meal.getAnalysisStatus()).isEqualTo(AnalysisStatus.PENDING);
        }

        @Test
        @DisplayName("AI 재분석이 비동기로 호출된다")
        void retakePhoto_ShouldTriggerAIReanalysisAsync() {
            // Given
            Meal meal = MealFixture.createDefaultBreakfast();
            MealRetakePhotoRequest request = new MealRetakePhotoRequest(
                    MultipartFileFixture.createValidJpegPhoto()
            );

            when(mealReader.findById(testMealId)).thenReturn(meal);
            when(reactionReader.existsByMealId(testMealId)).thenReturn(false);

            // When
            mealService.retakePhoto(testMemberId, testMealId, request);

            // Then
            verify(mealAnalysisService).analyzeMealAsync(eq(testMealId), any(MultipartFile.class));
        }
    }
}
