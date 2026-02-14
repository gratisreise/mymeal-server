package com.mymealserver.entity.enums;

import lombok.Getter;

@Getter
public enum WithdrawalReason {
    SERVICE_NOT_USEFUL("서비스가 도움이 되지 않음"),
    APP_ISSUES("앱 사용 불편"),
    PRIVACY_CONCERNS("개인정보 우려"),
    FOUND_ALTERNATIVE("다른 서비스 이용"),
    OTHER("기타");

    private final String description;

    WithdrawalReason(String description) {
        this.description = description;
    }
}
