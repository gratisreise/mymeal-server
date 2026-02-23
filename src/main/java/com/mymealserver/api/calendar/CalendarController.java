package com.mymealserver.api.calendar;

import com.mymealserver.api.calendar.dto.CalendarDailyResponse;
import com.mymealserver.api.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.api.calendar.service.CalendarService;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.mymealserver.common.annotation.AuthenticatedMember;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "캘린더")
@Validated
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/monthly")
    @Operation(summary = "월간 캘린더 조회", description = "특정 월의 식사 기록과 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<CalendarMonthlyResponse>> getMonthlyCalendar(
            @Parameter(description = "년도 (예: 2025)", required = true)
            @Min(value = 2020, message = "{validation.calendar.year.min}")
            @Max(value = 2100, message = "{validation.calendar.year.max}")
            @RequestParam Integer year,

            @Parameter(description = "월 (1-12)", required = true)
            @Min(value = 1, message = "{validation.calendar.month.min}")
            @Max(value = 12, message = "{validation.calendar.month.max}")
            @RequestParam Integer month,

            @AuthenticatedMember Long memberId
    ) {
        log.info("Getting monthly calendar for member: {}, year: {}, month: {}", memberId, year, month);
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(memberId, year, month);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/daily")
    @Operation(summary = "일별 캘린더 조회", description = "특정 날짜의 식사 기록과 상세 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<CalendarDailyResponse>> getDailyCalendar(
            @Parameter(description = "날짜 (예: 2025-02-09)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @AuthenticatedMember Long memberId
    ) {
        log.info("Getting daily calendar for member: {}, date: {}", memberId, date);
        CalendarDailyResponse response = calendarService.getDailyCalendar(memberId, date);
        return SuccessResponse.toOk(response);
    }
}
