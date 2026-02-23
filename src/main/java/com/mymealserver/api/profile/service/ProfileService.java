package com.mymealserver.api.profile.service;

import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.domain.member.Member;
import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
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

    /**
     * Get profile for authenticated member
     */
    public ProfileResponse getProfile(Long memberId) {
        log.debug("Getting profile for member: {}", memberId);
        Member member = memberReader.findById(memberId);

        return new ProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getProfileImage(),
                member.getCreatedAt()
        );
    }

    /**
     * Update profile for authenticated member
     * Only updates non-null fields
     */
    @Transactional
    public ProfileResponse updateProfile(Long memberId, UpdateProfileRequest request) {
        log.debug("Updating profile for member: {}", memberId);

        Member member = memberReader.findById(memberId);

        // Null-safe field updates
        if (request.name() != null && !request.name().isBlank()) {
            member.updateName(request.name());
        }

        // Allow clearing profile image by passing empty string
        if (request.profileImage() != null) {
            member.updateProfileImage(request.profileImage());
        }

        Member updatedMember = memberWriter.save(member);

        return new ProfileResponse(
                updatedMember.getId(),
                updatedMember.getEmail(),
                updatedMember.getName(),
                updatedMember.getProfileImage(),
                updatedMember.getCreatedAt()
        );
    }
}
