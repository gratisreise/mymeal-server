package com.mymealserver.api.calendar;

import com.mymealserver.api.calendar.dto.CalendarDailyResponse;
import com.mymealserver.api.calendar.dto.CalendarMonthlyResponse;
import com.mymealserver.api.calendar.service.CalendarService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Validated
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/monthly")
    @Operation(summary = "월간 캘린더 조회", description = "특정 월의 식사 기록과 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<CalendarMonthlyResponse>> getMonthlyCalendar(
            @Parameter(required = true)
            @Min(value = 2020, message = "{validation.calendar.year.min}")
            @Max(value = 2100, message = "{validation.calendar.year.max}")
            @RequestParam Integer year,

            @Parameter(required = true)
            @Min(value = 1, message = "{validation.calendar.month.min}")
            @Max(value = 12, message = "{validation.calendar.month.max}")
            @RequestParam Integer month,

            @CurrentMember Long memberId
    ) {
        CalendarMonthlyResponse response = calendarService.getMonthlyCalendar(memberId, year, month);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<SuccessResponse<CalendarDailyResponse>> getDailyCalendar(
            @Parameter(required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @CurrentMember Long memberId
    ) {
        CalendarDailyResponse response = calendarService.getDailyCalendar(memberId, date);
        return SuccessResponse.toOk(response);
    }
}
