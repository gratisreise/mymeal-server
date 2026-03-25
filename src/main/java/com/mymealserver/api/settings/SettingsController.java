package com.mymealserver.api.settings;

import com.mymealserver.api.profile.dto.request.UpdateNotificationRequest;
import com.mymealserver.api.settings.dto.response.SettingsResponse;
import com.mymealserver.api.settings.service.SettingsService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SuccessResponse<SettingsResponse>> getSettings(
            @Parameter(hidden = true)
            @CurrentMember Long memberId
    ) {
        return SuccessResponse.toOk(settingsService.getSettings(memberId));
    }

    @PutMapping("/notifications")
    public ResponseEntity<SuccessResponse<Void>> updateNotificationSettings(
            @Parameter(hidden = true)
            @CurrentMember Long memberId,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        settingsService.updateNotificationSettings(memberId, request);
        return SuccessResponse.toOk(null);
    }
}
