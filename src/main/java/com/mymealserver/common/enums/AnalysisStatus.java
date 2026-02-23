package com.mymealserver.common.enums;

import lombok.Getter;

@Getter
public enum AnalysisStatus {
    PENDING("대기 중"),
    PROCESSING("분석 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;

    AnalysisStatus(String description) {
        this.description = description;
    }
}
