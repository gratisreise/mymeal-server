package com.mymealserver.api.profile.service;

import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

  private final MemberReader memberReader;
  private final MemberWriter memberWriter;

  public ProfileResponse getProfile(Long memberId) {
    log.debug("회원 프로필 조회: memberId={}", memberId);
    Member member = memberReader.findById(memberId);

    return ProfileResponse.from(member);
  }

  @Transactional
  public ProfileResponse updateProfile(Long memberId, UpdateProfileRequest request) {
    log.debug("회원 프로필 수정: memberId={}", memberId);

    Member member = memberReader.findById(memberId);

    // 이름 업데이트 (null 및 빈 문자열 체크)
    if (request.name() != null && !request.name().isBlank()) {
      member.updateName(request.name());
    }

    // 프로필 이미지 업데이트 (null이 아닐 때만)
    if (request.profileImage() != null) {
      member.updateProfileImage(request.profileImage());
    }

    Member updatedMember = memberWriter.save(member);

    return ProfileResponse.from(updatedMember);
  }
}
