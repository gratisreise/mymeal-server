package com.mymealserver.api.ranking;

import com.mymealserver.common.response.PageResponse;
import com.mymealserver.common.response.SuccessResponse;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.api.ranking.dto.response.RankingItemResponse;
import com.mymealserver.api.ranking.service.DateRange;
import com.mymealserver.api.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "랭킹 API")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/best")
    @Operation(summary = "최고 식사 랭킹 조회", description = "점수가 높은 순으로 식사 랭킹을 조회합니다")
    public ResponseEntity<SuccessResponse<PageResponse<RankingItemResponse>>> getBestRanking(
            @Parameter(description = "회원 ID", required = true)
            @RequestParam Long memberId,

            @Parameter(description = "식사 유형 (BREAKFAST, LUNCH, DINNER, SNACK)")
            @RequestParam(required = false) MealType mealType,

            @Parameter(description = "시작일 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "종료일 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer size
    ) {
        log.info("getBestRanking called - memberId: {}, mealType: {}, dateRange: {} to {}, page: {}, size: {}",
                memberId, mealType, startDate, endDate, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("mealTime").descending());
        DateRange dateRange = (startDate != null || endDate != null)
                ? new DateRange(startDate, endDate)
                : null;

        PageResponse<RankingItemResponse> response = rankingService.getBestRanking(
                memberId, mealType, dateRange, pageable
        );

        return SuccessResponse.toOk(response);
    }

    @GetMapping("/worst")
    @Operation(summary = "최저 식사 랭킹 조회", description = "점수가 낮은 순으로 식사 랭킹을 조회합니다")
    public ResponseEntity<SuccessResponse<PageResponse<RankingItemResponse>>> getWorstRanking(
            @Parameter(description = "회원 ID", required = true)
            @RequestParam Long memberId,

            @Parameter(description = "식사 유형 (BREAKFAST, LUNCH, DINNER, SNACK)")
            @RequestParam(required = false) MealType mealType,

            @Parameter(description = "시작일 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "종료일 (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer size
    ) {
        log.info("getWorstRanking called - memberId: {}, mealType: {}, dateRange: {} to {}, page: {}, size: {}",
                memberId, mealType, startDate, endDate, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("mealTime").descending());
        DateRange dateRange = (startDate != null || endDate != null)
                ? new DateRange(startDate, endDate)
                : null;

        PageResponse<RankingItemResponse> response = rankingService.getWorstRanking(
                memberId, mealType, dateRange, pageable
        );

        return SuccessResponse.toOk(response);
    }
}
