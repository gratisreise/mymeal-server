package com.mymealserver.api.profile;

import com.mymealserver.common.response.SuccessResponse;
import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import com.mymealserver.api.profile.dto.response.StatisticsResponse;
import com.mymealserver.api.profile.service.BodyPatternService;
import com.mymealserver.api.profile.service.ProfileService;
import com.mymealserver.api.profile.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import com.mymealserver.common.annotation.AuthenticatedMember;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필/통계")
public class ProfileController {

    private final ProfileService profileService;
    private final StatisticsService statisticsService;
    private final BodyPatternService bodyPatternService;

    @GetMapping
    @Operation(summary = "프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile(
            @AuthenticatedMember Long memberId
    ) {
        log.info("Getting profile for member: {}", memberId);
        ProfileResponse response = profileService.getProfile(memberId);
        return SuccessResponse.toOk(response);
    }

    @PutMapping
    @Operation(summary = "프로필 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<SuccessResponse<ProfileResponse>> updateProfile(
            @AuthenticatedMember Long memberId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("Updating profile for member: {}", memberId);
        ProfileResponse response = profileService.updateProfile(memberId, request);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "통계 조회", description = "로그인한 사용자의 식사 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<StatisticsResponse>> getStatistics(
            @AuthenticatedMember Long memberId
    ) {
        log.info("Getting statistics for member: {}", memberId);
        StatisticsResponse response = statisticsService.getStatistics(memberId);
        return SuccessResponse.toOk(response);
    }

    @GetMapping("/patterns")
    @Operation(summary = "내 몸의 패턴", description = "로그인한 사용자의 몸의 패턴(좋은/나쁜 태그)을 조회합니다.")
    public ResponseEntity<SuccessResponse<BodyPatternResponse>> getBodyPatterns(
            @AuthenticatedMember Long memberId
    ) {
        log.info("Getting body patterns for member: {}", memberId);
        BodyPatternResponse response = bodyPatternService.getBodyPatterns(memberId);
        return SuccessResponse.toOk(response);
    }
}
