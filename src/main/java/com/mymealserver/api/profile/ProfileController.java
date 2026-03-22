package com.mymealserver.api.profile;

import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import com.mymealserver.api.profile.dto.response.StatisticsResponse;
import com.mymealserver.api.profile.service.BodyPatternService;
import com.mymealserver.api.profile.service.ProfileService;
import com.mymealserver.api.profile.service.StatisticsService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.SuccessResponse;
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
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final StatisticsService statisticsService;
    private final BodyPatternService bodyPatternService;

    @GetMapping
    public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile(
            @CurrentMember Long memberId
    ) {
        ProfileResponse response = profileService.getProfile(memberId);
        return SuccessResponse.toOk(response);
    }

    @PutMapping
    public ResponseEntity<SuccessResponse<ProfileResponse>> updateProfile(
            @CurrentMember Long memberId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        ProfileResponse response = profileService.updateProfile(memberId, request);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<StatisticsResponse>> getStatistics(
            @CurrentMember Long memberId
    ) {
        StatisticsResponse response = statisticsService.getStatistics(memberId);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/patterns")
    public ResponseEntity<SuccessResponse<BodyPatternResponse>> getBodyPatterns(
            @CurrentMember Long memberId
    ) {
        BodyPatternResponse response = bodyPatternService.getBodyPatterns(memberId);
        return SuccessResponse.toOk(response);
    }
}
