package com.mymealserver.api.profile;

import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.BodyPatternResponse;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import com.mymealserver.api.profile.dto.response.StatisticsResponse;
import com.mymealserver.api.profile.service.BodyPatternService;
import com.mymealserver.api.profile.service.ProfileService;
import com.mymealserver.api.profile.service.StatisticsService;
import com.mymealserver.common.annotation.AuthenticatedMember;
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
  public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile(@AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(profileService.getProfile(memberId));
  }

  @PutMapping
  public ResponseEntity<SuccessResponse<ProfileResponse>> updateProfile(
      @AuthenticatedMember Long memberId, @Valid @RequestBody UpdateProfileRequest request) {
    return SuccessResponse.toOk(profileService.updateProfile(memberId, request));
  }

  @GetMapping("/statistics")
  public ResponseEntity<SuccessResponse<StatisticsResponse>> getStatistics(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(statisticsService.getStatistics(memberId));
  }

  @GetMapping("/patterns")
  public ResponseEntity<SuccessResponse<BodyPatternResponse>> getBodyPatterns(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(bodyPatternService.getBodyPatterns(memberId));
  }
}
