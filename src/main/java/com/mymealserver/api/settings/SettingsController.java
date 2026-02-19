package com.mymealserver.api.settings;

import com.mymealserver.api.profile.dto.request.UpdateNotificationRequest;
import com.mymealserver.api.settings.dto.response.SettingsResponse;
import com.mymealserver.api.settings.service.SettingsService;
import com.mymealserver.common.annotation.AuthenticatedMember;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "사용자 설정 관리")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "사용자 설정 조회", description = "알림 설정 및 식사 시간을 조회합니다")
    public ResponseEntity<SuccessResponse<SettingsResponse>> getSettings(
            @Parameter(hidden = true)
            @AuthenticatedMember Long memberId
    ) {
        log.info("getSettings called - memberId: {}", memberId);

        SettingsResponse response = settingsService.getSettings(memberId);
        return SuccessResponse.toOk(response);
    }

    @PutMapping("/notifications")
    @Operation(summary = "알림 설정 수정", description = "알림 및 식사 시간 설정을 수정합니다")
    public ResponseEntity<SuccessResponse<Void>> updateNotificationSettings(
            @Parameter(hidden = true)
            @AuthenticatedMember Long memberId,

            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        log.info("updateNotificationSettings called - memberId: {}", memberId);

        settingsService.updateNotificationSettings(memberId, request);
        return SuccessResponse.toOk(null);
    }
}
