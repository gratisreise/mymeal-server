package com.mymealserver.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // ========================================================================
    // AUTH - 인증 관련
    // ========================================================================
    INVALID_CREDENTIALS(401, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(401, "AUTH_002", "액세스 토큰이 만료되었습니다."),
    TOKEN_INVALID(401, "AUTH_003", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_INVALID(401, "AUTH_004", "리프레시 토큰이 유효하지 않습니다."),
    OAUTH_TOKEN_FAILED(401, "AUTH_005", "소셜 로그인 토큰 검증에 실패했습니다."),
    OAUTH_UNSUPPORTED_PROVIDER(400, "AUTH_006", "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_API_FAILED(502, "AUTH_009", "OAuth API 호출에 실패했습니다."),
    OAUTH_USER_INFO_FAILED(502, "AUTH_010", "사용자 정보 조회에 실패했습니다."),
    INVALID_REDIRECT_URI(400, "AUTH_011", "허용되지 않은 리다이렉트 URI입니다."),
    ALREADY_LOGGED_IN(400, "AUTH_007", "이미 로그인된 상태입니다."),
    NOT_LOGGED_IN(401, "AUTH_008", "로그인이 필요한 서비스입니다."),

    // ========================================================================
    // MEMBER - 회원 관련
    // ========================================================================
    MEMBER_NOT_FOUND(404, "MEMBER_001", "사용자를 찾을 수 없습니다."),
    MEMBER_EMAIL_ALREADY_EXISTS(409, "MEMBER_002", "이미 사용 중인 이메일입니다."),
    MEMBER_WITHDRAWN(410, "MEMBER_003", "이미 탈퇴한 회원입니다."),
    MEMBER_NAME_REQUIRED(400, "MEMBER_004", "이름은 필수 입력 항목입니다."),
    MEMBER_PROFILE_IMAGE_INVALID(400, "MEMBER_005", "프로필 이미지 URL이 유효하지 않습니다."),
    MEMBER_DEACTIVATED(403, "MEMBER_006", "비활성화된 회원입니다."),

    // ========================================================================
    // MEAL - 식사 관련
    // ========================================================================
    MEAL_NOT_FOUND(404, "MEAL_001", "식사 기록을 찾을 수 없습니다."),
    MEAL_PHOTO_REQUIRED(400, "MEAL_002", "식사 사진은 필수 항목입니다."),
    MEAL_PHOTO_SIZE_EXCEEDED(400, "MEAL_003", "사진 파일 크기는 10MB를 초과할 수 없습니다."),
    MEAL_PHOTO_FORMAT_INVALID(400, "MEAL_004", "지원하지 않는 이미지 형식입니다. (JPEG, PNG, HEIC만 가능)"),
    MEAL_TYPE_INVALID(400, "MEAL_005", "잘못된 식사 유형입니다."),
    MEAL_TIME_INVALID(400, "MEAL_006", "식사 시간이 유효하지 않습니다."),
    MEAL_MEMO_TOO_LONG(400, "MEAL_007", "메모는 500자 이내로 작성해야 합니다."),
    MEAL_AI_ANALYSIS_FAILED(500, "MEAL_008", "AI 이미지 분석에 실패했습니다. 나중에 다시 시도해주세요."),
    AI_ANALYSIS_ERROR(500, "AI_001", "AI 분석에 실패했습니다. 나중에 다시 시도해주세요."),
    MEAL_ALREADY_EXISTS(409, "MEAL_009", "이미 해당 시간에 식사 기록이 존재합니다."),
    MEAL_FORBIDDEN(403, "MEAL_010", "다른 사용자의 식사 기록에 접근할 권한이 없습니다."),
    MEAL_CANNOT_UPDATE(403, "MEAL_011", "다른 사용자의 식사 기록은 수정할 수 없습니다."),
    MEAL_CANNOT_DELETE(403, "MEAL_012", "다른 사용자의 식사 기록은 삭제할 수 없습니다."),
    MEAL_LOG_NOT_FOUND(404, "MEAL_013", "식사 로그를 찾을 수 없습니다."),

    // ========================================================================
    // REACTION - 식후 반응 관련
    // ========================================================================
    REACTION_NOT_FOUND(404, "REACTION_001", "식후 반응 기록을 찾을 수 없습니다."),
    REACTION_ALREADY_EXISTS(409, "REACTION_002", "이미 식후 반응이 기록되어 있습니다."),
    REACTION_DIGESTION_LEVEL_INVALID(400, "REACTION_003", "소화 상태는 1~5 사이 값이어야 합니다."),
    REACTION_FULLNESS_LEVEL_INVALID(400, "REACTION_004", "포만감은 1~5 사이 값이어야 합니다."),
    REACTION_ENERGY_LEVEL_INVALID(400, "REACTION_005", "에너지 레벨은 1~5 사이 값이어야 합니다."),
    REACTION_MEMO_TOO_LONG(400, "REACTION_006", "메모는 500자 이내로 작성해야 합니다."),
    REACTION_TOO_EARLY(400, "REACTION_007", "식사 후 1시간이 지나지 않아 반응을 기록할 수 없습니다."),
    REACTION_REMINDER_EXHAUSTED(400, "REACTION_008", "더 이상 나중에 하기를 요청할 수 없습니다."),

    // ========================================================================
    // NOTIFICATION - 알림 관련
    // ========================================================================
    NOTIFICATION_NOT_FOUND(404, "NOTI_001", "알림을 찾을 수 없습니다."),
    NOTIFICATION_FCM_TOKEN_INVALID(400, "NOTI_002", "FCM 토큰이 유효하지 않습니다."),
    NOTIFICATION_TIME_INVALID(400, "NOTI_003", "알림 시간 형식이 올바르지 않습니다. (HH:mm 형식 required)"),
    NOTIFICATION_TYPE_INVALID(400, "NOTI_004", "알림 유형이 유효하지 않습니다."),

    // ========================================================================
    // CALENDAR - 캘린더 관련
    // ========================================================================
    CALENDAR_DATE_INVALID(400, "CAL_001", "날짜 형식이 올바르지 않습니다."),
    CALENDAR_MONTH_INVALID(400, "CAL_002", "월은 1~12 사이 값이어야 합니다."),
    CALENDAR_YEAR_INVALID(400, "CAL_003", "년도는 유효한 범위 내여야 합니다."),
    CALENDAR_DATE_RANGE_INVALID(400, "CAL_004", "시작일은 종료일보다 이전이어야 합니다."),

    // ========================================================================
    // RECOMMENDATION - 추천 관련
    // ========================================================================
    RECOMMENDATION_NOT_FOUND(404, "REC_001", "추천 정보를 찾을 수 없습니다."),
    RECOMMENDATION_INSUFFICIENT_DATA(400, "REC_002", "추천을 생성하기 위한 데이터가 부족합니다. 식사를 더 기록해주세요."),
    RECOMMENDATION_LIMIT_INVALID(400, "REC_003", "추천 요청 수량은 1~10 사이여야 합니다."),

    // ========================================================================
    // FOOD - 음식 관련
    // ========================================================================
    FOOD_NOT_FOUND(404, "FOOD_001", "음식을 찾을 수 없습니다."),

    // ========================================================================
    // VALIDATION - 입력 검증
    // ========================================================================
    EMAIL_FORMAT_INVALID(400, "VAL_001", "이메일 형식이 올바르지 않습니다."),
    PASSWORD_TOO_SHORT(400, "VAL_002", "비밀번호는 최소 8자 이상이어야 합니다."),
    PASSWORD_TOO_WEAK(400, "VAL_003", "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."),
    NAME_TOO_LONG(400, "VAL_004", "이름은 50자 이내로 입력해야 합니다."),
    TERMS_AGREEMENT_REQUIRED(400, "VAL_005", "이용약관 동의는 필수 항목입니다."),
    PRIVACY_AGREEMENT_REQUIRED(400, "VAL_006", "개인정보 처리방침 동의는 필수 항목입니다."),
    WITHDRAW_REASON_REQUIRED(400, "VAL_007", "탈퇴 사유는 필수 항목입니다."),
    PAGE_SIZE_EXCEEDED(400, "VAL_008", "페이지 크기는 최대 100까지 가능합니다."),
    PAGE_NUMBER_INVALID(400, "VAL_009", "페이지 번호는 0 이상이어야 합니다."),
    TAG_FORMAT_INVALID(400, "VAL_010", "태그 형식이 올바르지 않습니다."),
    PERIOD_INVALID(400, "VAL_011", "기간 설정이 올바르지 않습니다. (ALL, WEEKLY, MONTHLY)"),
    SORT_TYPE_INVALID(400, "VAL_012", "정렬 방식이 올바르지 않습니다."),
    FILE_UPLOAD_FAILED(400, "VAL_013", "파일 업로드에 실패했습니다."),
    REQUEST_BODY_MISSING(400, "VAL_014", "요청 본문이 누락되었습니다."),
    PATH_VARIABLE_MISSING(400, "VAL_015", "필수 경로 변수가 누락되었습니다."),
    QUERY_PARAMETER_MISSING(400, "VAL_016", "필수 쿼리 파라미터가 누락되었습니다."),
    DATE_FORMAT_INVALID(400, "VAL_017", "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    DATE_TIME_FORMAT_INVALID(400, "VAL_018", "날짜시간 형식이 올바르지 않습니다. (yyyy-MM-dd'T'HH:mm:ss)"),

    // ========================================================================
    // SETTINGS - 설정 관련
    // ========================================================================
    SETTINGS_NOT_FOUND(404, "SET_001", "설정을 찾을 수 없습니다."),
    SETTINGS_MEAL_TIME_DUPLICATE(400, "SET_002", "식사 시간이 중복되었습니다."),
    SETTINGS_MEAL_TIME_INVALID(400, "SET_003", "식사 시간은 00:00~23:59 사이여야 합니다."),

    // ========================================================================
    // EXPORT - 내보내기 관련
    // ========================================================================
    EXPORT_FORMAT_INVALID(400, "EXP_001", "내보내기 형식이 올바르지 않습니다. (CSV, PDF)"),
    EXPORT_FAILED(500, "EXP_002", "내보내기에 실패했습니다."),

    // ========================================================================
    // SEARCH - 검색 관련
    // ========================================================================
    SEARCH_KEYWORD_TOO_SHORT(400, "SEARCH_001", "검색어는 최소 2자 이상이어야 합니다."),
    SEARCH_RESULT_EMPTY(404, "SEARCH_002", "검색 결과가 없습니다."),

    // ========================================================================
    // RANKING - 랭킹 관련
    // ========================================================================
    RANKING_MEAL_NOT_FOUND(404, "RANK_001", "랭킹에 포함될 식사를 찾을 수 없습니다."),
    RANKING_LIMIT_INVALID(400, "RANK_002", "랭킹 조회 수량은 1~50 사이여야 합니다."),

    // ========================================================================
    // SYSTEM - 시스템 관련
    // ========================================================================
    INTERNAL_SERVER_ERROR(500, "SYS_001", "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(500, "SYS_002", "데이터베이스 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(502, "SYS_003", "외부 API 호출에 실패했습니다."),
    FILE_STORAGE_ERROR(500, "SYS_004", "파일 저장에 실패했습니다."),
    RATE_LIMIT_EXCEEDED(429, "SYS_005", "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    SERVICE_UNAVAILABLE(503, "SYS_006", "서비스를 일시적으로 사용할 수 없습니다."),

    // ========================================================================
    // SECURITY - 보안 관련
    // ========================================================================
    ACCESS_DENIED(403, "SEC_001", "접근 권한이 없습니다."),
    CSRF_TOKEN_INVALID(403, "SEC_002", "CSRF 토큰이 유효하지 않습니다."),
    SUSPICIOUS_ACTIVITY_DETECTED(403, "SEC_003", "의심스러운 활동이 감지되어 계정이 일시적으로 제한되었습니다."),

    // ========================================================================
    // 미처리 오류
    // ========================================================================
    UNKNOWN_ERROR(500, "UNKNOWN", "알 수 없는 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
