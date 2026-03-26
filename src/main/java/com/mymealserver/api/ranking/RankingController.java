package com.mymealserver.api.ranking;

import com.mymealserver.api.ranking.dto.response.RankingItemResponse;
import com.mymealserver.api.ranking.service.DateRange;
import com.mymealserver.api.ranking.service.RankingService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.response.PageResponse;
import com.mymealserver.common.response.SuccessResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  @GetMapping("/best")
  public ResponseEntity<SuccessResponse<PageResponse<RankingItemResponse>>> getBestRanking(
      @CurrentMember Long memberId,
      @RequestParam(required = false) MealType mealType,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
      @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("mealTime").descending());
    DateRange dateRange =
        (startDate != null && endDate != null) ? DateRange.of(startDate, endDate) : null;
    PageResponse<RankingItemResponse> response =
        rankingService.getBestRanking(memberId, mealType, dateRange, pageable);
    return SuccessResponse.toOk(response);
  }

  @GetMapping("/worst")
  public ResponseEntity<SuccessResponse<PageResponse<RankingItemResponse>>> getWorstRanking(
      @CurrentMember Long memberId,
      @RequestParam MealType mealType,
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("mealTime").descending());
    DateRange dateRange =
        (startDate != null && endDate != null) ? DateRange.of(startDate, endDate) : null;
    PageResponse<RankingItemResponse> response =
        rankingService.getWorstRanking(memberId, mealType, dateRange, pageable);
    return SuccessResponse.toOk(response);
  }
}
