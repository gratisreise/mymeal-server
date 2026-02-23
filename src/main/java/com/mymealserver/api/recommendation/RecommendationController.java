package com.mymealserver.api.recommendation;

import com.mymealserver.api.recommendation.dto.response.RecommendationResponse;
import com.mymealserver.api.recommendation.dto.response.RecommendationScheduleResponse;
import com.mymealserver.api.recommendation.service.RecommendationService;
import com.mymealserver.common.annotation.AuthenticatedMember;
import com.mymealserver.common.response.SuccessResponse;
import com.mymealserver.common.enums.MealType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "식단 추천")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "식단 추천 조회", description = "과거 식사 반응을 기반으로 식단을 추천합니다")
    public ResponseEntity<SuccessResponse<List<RecommendationResponse>>> getRecommendations(
            @Parameter(description = "인증된 회원 ID", hidden = true)
            @AuthenticatedMember Long memberId,

            @Parameter(description = "식사 유형 (BREAKFAST, LUNCH, DINNER, SNACK)")
            @RequestParam(required = false) MealType mealType,

            @Parameter(description = "추천 수량 (1~10, 기본값 3)")
            @RequestParam(required = false) Integer limit
    ) {
        log.info("getRecommendations called - memberId: {}, mealType: {}, limit: {}",
                memberId, mealType, limit);

        List<RecommendationResponse> response = recommendationService.getRecommendations(
                memberId, mealType, limit
        );

        return SuccessResponse.toOk(response);
    }

    @GetMapping("/schedule")
    @Operation(summary = "추천 스케줄 조회", description = "식사 시간 기반 추천 알림 스케줄을 조회합니다")
    public ResponseEntity<SuccessResponse<List<RecommendationScheduleResponse>>> getRecommendationSchedule(
            @Parameter(description = "인증된 회원 ID", hidden = true)
            @AuthenticatedMember Long memberId
    ) {
        log.info("getRecommendationSchedule called - memberId: {}", memberId);

        List<RecommendationScheduleResponse> response = recommendationService.getRecommendationSchedule(memberId);

        return SuccessResponse.toOk(response);
    }
}
