package com.mymealserver.api.recommendation;

import com.mymealserver.api.recommendation.dto.response.RecommendationResponse;
import com.mymealserver.api.recommendation.dto.response.RecommendationScheduleResponse;
import com.mymealserver.api.recommendation.service.RecommendationService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<RecommendationResponse>>> getRecommendations(
            @CurrentMember Long memberId,
            @RequestParam MealType mealType,
            @RequestParam Integer limit
    ) {
        return SuccessResponse.toOk(recommendationService.getRecommendations(memberId, mealType, limit));
    }

    @GetMapping("/schedule")
    @Operation(summary = "추천 스케줄 조회", description = "식사 시간 기반 추천 알림 스케줄을 조회합니다")
    public ResponseEntity<SuccessResponse<List<RecommendationScheduleResponse>>> getRecommendationSchedule(
            @Parameter(description = "인증된 회원 ID", hidden = true)
            @CurrentMember Long memberId
    ) {
        return SuccessResponse.toOk(recommendationService.getRecommendationSchedule(memberId));
    }
}
