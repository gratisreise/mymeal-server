package com.mymealserver.api.meal;

import com.mymealserver.api.meal.dto.response.MealDetailResponse;
import com.mymealserver.api.meal.dto.response.MealResponse;
import com.mymealserver.api.meal.service.MealService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
@Validated
public class MealController {

    private final MealService mealService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<MealResponse>> createMeal(
            @CurrentMember Long memberId,
            @Parameter(required = true)
            @RequestParam MultipartFile photo,
            @Parameter(required = true)
            @RequestParam MealType mealType
    ) {
        MealResponse response = mealService.createMeal(memberId, photo, mealType);
        return SuccessResponse.toCreated(response);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<MealResponse>>> getMeals(
            @CurrentMember Long memberId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) MealType mealType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MealResponse> meals = mealService.getMeals(memberId, startDate, endDate, mealType, pageable);
        return SuccessResponse.toOk(PageResponse.from(meals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<MealDetailResponse>> getMealDetail(
            @CurrentMember Long memberId,
            @PathVariable Long id
    ) {
        MealDetailResponse response = mealService.getMealDetail(memberId, id);
        return SuccessResponse.toOk(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @CurrentMember Long memberId,
            @PathVariable Long id
    ) {
        mealService.deleteMeal(memberId, id);
        return SuccessResponse.toOk(null);
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<MealResponse>> retakePhoto(
            @CurrentMember Long memberId,
            @PathVariable Long id,
            @Parameter(required = true)
            @RequestParam MultipartFile photo
    ) {
        MealResponse response = mealService.retakePhoto(memberId, id, photo);
        return SuccessResponse.toOk(response);
    }
}
