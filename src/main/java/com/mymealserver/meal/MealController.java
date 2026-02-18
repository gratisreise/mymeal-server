package com.mymealserver.meal;

import com.mymealserver.common.response.PageResponse;
import com.mymealserver.common.response.SuccessResponse;
import com.mymealserver.entity.Meal;
import com.mymealserver.entity.enums.MealType;
import com.mymealserver.meal.dto.request.MealCreateRequest;
import com.mymealserver.meal.dto.request.MealRetakePhotoRequest;
import com.mymealserver.meal.dto.response.MealDetailResponse;
import com.mymealserver.meal.dto.response.MealResponse;
import com.mymealserver.meal.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import com.mymealserver.common.annotation.AuthenticatedMember;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
@Tag(name = "Meals", description = "식사 관리")
@Validated
public class MealController {

    private final MealService mealService;

    @PostMapping
    @Operation(summary = "식사 생성", description = "사진을 업로드하고 식사를 저장합니다. AI 음식 분석이 백그라운드에서 진행됩니다.")
    public ResponseEntity<SuccessResponse<MealResponse>> createMeal(
            @AuthenticatedMember Long memberId,
            @Valid @ModelAttribute MealCreateRequest request
    ) {
        MealResponse response = mealService.createMeal(memberId, request);
        return SuccessResponse.toCreated(response);
    }

    @GetMapping
    @Operation(summary = "식사 목록 조회", description = "페이지네이션과 필터링을 지원합니다.")
    public ResponseEntity<SuccessResponse<PageResponse<MealResponse>>> getMeals(
            @AuthenticatedMember Long memberId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) MealType mealType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MealResponse> meals = mealService.getMeals(memberId, startDate, endDate, mealType, pageable);
        return SuccessResponse.toOk(PageResponse.from(meals));
    }

    @GetMapping("/{id}")
    @Operation(summary = "식사 상세 조회", description = "AI 분석 결과와 식후 반응을 포함한 상세 정보를 조회합니다.")
    public ResponseEntity<SuccessResponse<MealDetailResponse>> getMealDetail(
            @AuthenticatedMember Long memberId,
            @PathVariable Long id
    ) {
        MealDetailResponse response = mealService.getMealDetail(memberId, id);
        return SuccessResponse.toOk(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "식사 삭제", description = "식사를 삭제합니다. 관련된 식후 반응 데이터는 보존됩니다.")
    public ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @AuthenticatedMember Long memberId,
            @PathVariable Long id
    ) {
        mealService.deleteMeal(memberId, id);
        return SuccessResponse.toOk(null);
    }

    @PostMapping("/{id}/photo")
    @Operation(summary = "사진 재촬영", description = "기존 식사의 사진을 새로운 사진으로 교체하고 AI 재분석을 진행합니다. 기존 식후 반응 데이터는 보존됩니다.")
    public ResponseEntity<SuccessResponse<MealResponse>> retakePhoto(
            @AuthenticatedMember Long memberId,
            @PathVariable Long id,
            @Valid @ModelAttribute MealRetakePhotoRequest request
    ) {
        MealResponse response = mealService.retakePhoto(memberId, id, request);
        return SuccessResponse.toOk(response);
    }
}
