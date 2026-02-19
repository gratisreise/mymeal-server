package com.mymealserver.api.calendar.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarWriter {
    // 향후 캘린더 설정, 선호도 업데이트 등의 기능 확장을 위해 준비된 클래스
}
