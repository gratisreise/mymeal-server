package com.mymealserver.calendar.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calendar Domain Writer
 * 캘린더 관련 명령 로직을 담당하는 Domain Writer
 * 향후 캘린더 설정, 선호도 업데이트 등의 기능 확장을 위해 준비된 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CalendarWriter {
    // Placeholder for future calendar preference updates, etc.
}
