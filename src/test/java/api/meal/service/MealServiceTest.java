package api.meal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.mymealserver.api.meal.dto.response.MealDetailResponse;
import com.mymealserver.api.meal.dto.response.MealResponse;
import com.mymealserver.api.meal.service.MealAnalysisService;
import com.mymealserver.api.meal.service.MealService;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.meal.MealWriter;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.external.redis.NotificationPayload;
import com.mymealserver.external.redis.UnifiedNotificationService;
import com.mymealserver.external.s3.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

  @Mock private MealReader mealReader;
  @Mock private MealWriter mealWriter;
  @Mock private ReactionReader reactionReader;
  @Mock private MealAnalysisReader mealAnalysisReader;
  @Mock private S3Service s3Service;
  @Mock private MealAnalysisService mealAnalysisService;
  @Mock private UnifiedNotificationService unifiedNotificationService;

  @InjectMocks private MealService mealService;

  private static final Long MEMBER_ID = 1L;
  private static final Long OTHER_MEMBER_ID = 2L;

  // ========================
  // createMeal
  // ========================

  @Test
  void createMeal_success() {
    // given
    MockMultipartFile photo =
        new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test image".getBytes());

    given(s3Service.uploadMealPhoto(photo, MEMBER_ID))
        .willReturn("https://s3.example.com/photo.jpg");
    given(s3Service.extractPhotoKey("https://s3.example.com/photo.jpg")).willReturn("photo.jpg");

    Meal savedMeal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.PENDING);
    given(mealWriter.save(any(Meal.class))).willReturn(savedMeal);

    // when
    MealResponse response = mealService.createMeal(MEMBER_ID, photo, MealType.LUNCH);

    // then
    assertThat(response).isNotNull();
    assertThat(response.mealType()).isEqualTo(MealType.LUNCH);
    assertThat(response.hasReaction()).isFalse();

    then(mealWriter).should().save(any(Meal.class));
    then(mealAnalysisService)
        .should()
        .analyzeMealAsync(eq(1L), eq(MealType.LUNCH), any(Resource.class));
    then(unifiedNotificationService)
        .should()
        .schedule(any(NotificationPayload.class), any(LocalDateTime.class));
  }

  // ========================
  // getMeals
  // ========================

  @Test
  void getMeals_success_withPagination() {
    // given
    LocalDate startDate = LocalDate.of(2026, 4, 1);
    LocalDate endDate = LocalDate.of(2026, 4, 30);
    Pageable pageable = PageRequest.of(0, 10);

    Meal meal1 =
        createMeal(
            1L, MEMBER_ID, MealType.BREAKFAST, startDate.atTime(8, 0), AnalysisStatus.COMPLETED);
    Meal meal2 =
        createMeal(
            2L, MEMBER_ID, MealType.LUNCH, startDate.atTime(12, 0), AnalysisStatus.COMPLETED);

    Page<Meal> mealPage = new PageImpl<>(List.of(meal1, meal2), pageable, 2);

    given(mealReader.findByMemberId(MEMBER_ID, startDate, endDate, null, pageable))
        .willReturn(mealPage);
    given(reactionReader.existsByMealId(1L)).willReturn(true);
    given(reactionReader.existsByMealId(2L)).willReturn(false);

    // when
    PageResponse<MealResponse> response =
        mealService.getMeals(MEMBER_ID, startDate, endDate, null, pageable);

    // then
    assertThat(response.getData()).hasSize(2);
    assertThat(response.getData().get(0).hasReaction()).isTrue();
    assertThat(response.getData().get(1).hasReaction()).isFalse();
    assertThat(response.getPagination().totalElements()).isEqualTo(2);
  }

  @Test
  void getMeals_success_emptyPage() {
    // given
    LocalDate startDate = LocalDate.of(2026, 4, 1);
    LocalDate endDate = LocalDate.of(2026, 4, 30);
    Pageable pageable = PageRequest.of(0, 10);

    given(mealReader.findByMemberId(MEMBER_ID, startDate, endDate, null, pageable))
        .willReturn(new PageImpl<>(List.of(), pageable, 0));

    // when
    PageResponse<MealResponse> response =
        mealService.getMeals(MEMBER_ID, startDate, endDate, null, pageable);

    // then
    assertThat(response.getData()).isEmpty();
    assertThat(response.getPagination().totalElements()).isEqualTo(0);
  }

  @Test
  void getMeals_success_filteredByMealType() {
    // given
    LocalDate startDate = LocalDate.of(2026, 4, 1);
    LocalDate endDate = LocalDate.of(2026, 4, 30);
    Pageable pageable = PageRequest.of(0, 10);

    Meal meal =
        createMeal(
            1L, MEMBER_ID, MealType.DINNER, startDate.atTime(19, 0), AnalysisStatus.COMPLETED);
    Page<Meal> mealPage = new PageImpl<>(List.of(meal), pageable, 1);

    given(mealReader.findByMemberId(MEMBER_ID, startDate, endDate, MealType.DINNER, pageable))
        .willReturn(mealPage);
    given(reactionReader.existsByMealId(1L)).willReturn(false);

    // when
    PageResponse<MealResponse> response =
        mealService.getMeals(MEMBER_ID, startDate, endDate, MealType.DINNER, pageable);

    // then
    assertThat(response.getData()).hasSize(1);
    assertThat(response.getData().get(0).mealType()).isEqualTo(MealType.DINNER);
  }

  // ========================
  // getMealDetail
  // ========================

  @Test
  void getMealDetail_success_withAnalysisAndReaction() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    MealAnalysis analysis = createMealAnalysis(100L, 1L, "비빔밥");
    Reaction reaction = createReaction(10L, 1L, 4.5);

    given(mealReader.findById(1L)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(1L)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(1L)).willReturn(reaction);

    // when
    MealDetailResponse response = mealService.getMealDetail(MEMBER_ID, 1L);

    // then
    assertThat(response).isNotNull();
    assertThat(response.hasReaction()).isTrue();
    assertThat(response.aiAnalysis()).isNotNull();
    assertThat(response.aiAnalysis().mealName()).isEqualTo("비빔밥");
    assertThat(response.reaction()).isNotNull();
    assertThat(response.reaction().overallScore()).isEqualTo(4.5);
  }

  @Test
  void getMealDetail_success_analysisPending() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.PENDING);
    Reaction reaction = createReaction(10L, 1L, 3.0);

    given(mealReader.findById(1L)).willReturn(meal);
    given(reactionReader.findByMealId(1L)).willReturn(reaction);

    // when
    MealDetailResponse response = mealService.getMealDetail(MEMBER_ID, 1L);

    // then
    assertThat(response.aiAnalysis()).isNull();
    assertThat(response.reaction()).isNotNull();
    assertThat(response.hasReaction()).isTrue();
  }

  @Test
  void getMealDetail_success_noReaction() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    MealAnalysis analysis = createMealAnalysis(100L, 1L, "된장찌개");

    given(mealReader.findById(1L)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(1L)).willReturn(Optional.of(analysis));
    given(reactionReader.findByMealId(1L)).willReturn(null);

    // when
    MealDetailResponse response = mealService.getMealDetail(MEMBER_ID, 1L);

    // then
    assertThat(response.hasReaction()).isFalse();
    assertThat(response.reaction()).isNull();
    assertThat(response.aiAnalysis()).isNotNull();
  }

  @Test
  void getMealDetail_success_noAnalysisNoReaction() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.PENDING);

    given(mealReader.findById(1L)).willReturn(meal);
    given(reactionReader.findByMealId(1L)).willReturn(null);

    // when
    MealDetailResponse response = mealService.getMealDetail(MEMBER_ID, 1L);

    // then
    assertThat(response.aiAnalysis()).isNull();
    assertThat(response.reaction()).isNull();
    assertThat(response.hasReaction()).isFalse();
  }

  @Test
  void getMealDetail_fail_otherMembersMeal() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    given(mealReader.findById(1L)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> mealService.getMealDetail(OTHER_MEMBER_ID, 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);
  }

  @Test
  void getMealDetail_success_analysisCompletedButNotFound() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);

    given(mealReader.findById(1L)).willReturn(meal);
    given(mealAnalysisReader.findByMealId(1L)).willReturn(Optional.empty());
    given(reactionReader.findByMealId(1L)).willReturn(null);

    // when
    MealDetailResponse response = mealService.getMealDetail(MEMBER_ID, 1L);

    // then
    assertThat(response.aiAnalysis()).isNull();
    assertThat(response.hasReaction()).isFalse();
  }

  // ========================
  // retakePhoto
  // ========================

  @Test
  void retakePhoto_success() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    MockMultipartFile newPhoto =
        new MockMultipartFile("photo", "new.jpg", "image/jpeg", "new image".getBytes());

    given(mealReader.findById(1L)).willReturn(meal);
    given(s3Service.uploadMealPhoto(newPhoto, MEMBER_ID))
        .willReturn("https://s3.example.com/new-photo.jpg");
    given(s3Service.extractPhotoKey("https://s3.example.com/new-photo.jpg"))
        .willReturn("new-photo.jpg");
    given(mealWriter.save(any(Meal.class))).willReturn(meal);
    given(reactionReader.existsByMealId(1L)).willReturn(true);

    // when
    MealResponse response = mealService.retakePhoto(MEMBER_ID, 1L, newPhoto);

    // then
    assertThat(response).isNotNull();
    then(s3Service).should().deletePhoto("photo1.jpg");
    then(mealAnalysisService)
        .should()
        .analyzeMealAsync(eq(1L), eq(MealType.LUNCH), any(Resource.class));
    assertThat(response.hasReaction()).isTrue();
  }

  @Test
  void retakePhoto_fail_otherMembersMeal() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    MockMultipartFile photo =
        new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test".getBytes());

    given(mealReader.findById(1L)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> mealService.retakePhoto(OTHER_MEMBER_ID, 1L, photo))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);

    then(s3Service).should(never()).uploadMealPhoto(any(), any());
  }

  // ========================
  // deleteMeal
  // ========================

  @Test
  void deleteMeal_success() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    given(mealReader.findById(1L)).willReturn(meal);

    // when
    mealService.deleteMeal(MEMBER_ID, 1L);

    // then
    then(mealWriter).should().delete(meal);
  }

  @Test
  void deleteMeal_fail_otherMembersMeal() {
    // given
    Meal meal =
        createMeal(1L, MEMBER_ID, MealType.LUNCH, LocalDateTime.now(), AnalysisStatus.COMPLETED);
    given(mealReader.findById(1L)).willReturn(meal);

    // when & then
    assertThatThrownBy(() -> mealService.deleteMeal(OTHER_MEMBER_ID, 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(ErrorCode.MEAL_FORBIDDEN);

    then(mealWriter).should(never()).delete(any());
  }

  // --- Helper methods ---

  private Meal createMeal(
      Long id, Long memberId, MealType mealType, LocalDateTime mealTime, AnalysisStatus status) {
    return Meal.builder()
        .id(id)
        .memberId(memberId)
        .mealType(mealType)
        .mealTime(mealTime)
        .photoUrl("https://s3.example.com/photo" + id + ".jpg")
        .photoKey("photo" + id + ".jpg")
        .analysisStatus(status)
        .build();
  }

  private MealAnalysis createMealAnalysis(Long id, Long mealId, String mealName) {
    return MealAnalysis.builder()
        .id(id)
        .mealId(mealId)
        .mealName(mealName)
        .calories(500.0)
        .carbohydrates(60.0)
        .protein(30.0)
        .fat(15.0)
        .confidence(0.95)
        .build();
  }

  private Reaction createReaction(Long id, Long mealId, Double overallScore) {
    Reaction reaction =
        Reaction.builder()
            .id(id)
            .mealId(mealId)
            .digestionLevel((short) 4)
            .fullnessLevel((short) 3)
            .energyLevel((short) 4)
            .build();
    reaction.setOverallScore(overallScore);
    return reaction;
  }
}
