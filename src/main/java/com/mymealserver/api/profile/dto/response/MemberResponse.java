package com.mymealserver.api.profile.dto.response;

import com.mymealserver.domain.member.Member;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MemberResponse(
    Long id, String email, String name, String profileImage, LocalDateTime createdAt) {

  public static MemberResponse from(Member member) {
    return MemberResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .name(member.getName())
        .profileImage(member.getProfileImage())
        .createdAt(member.getCreatedAt())
        .build();
  }
}
