package com.mymealserver.profile.service;

import com.mymealserver.api.profile.service.ProfileService;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.test.fixtures.MemberFixture;
import com.mymealserver.common.test.fixtures.ProfileFixture;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import com.mymealserver.entity.Member;
import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService 단위 테스트")
class ProfileServiceTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private MemberWriter memberWriter;

    @InjectMocks
    private ProfileService profileService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = MemberFixture.createDefaultMember();
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetProfileTests {

        @Test
        @DisplayName("존재하는 회원 ID로 프로필 조회에 성공한다")
        void getProfile_WithExistingMemberId_ShouldReturnProfile() {
            // Given
            Long memberId = 1L;
            when(memberReader.findById(memberId)).thenReturn(testMember);

            // When
            ProfileResponse response = profileService.getProfile(memberId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(memberId);
            assertThat(response.email()).isEqualTo(testMember.getEmail());
            assertThat(response.name()).isEqualTo(testMember.getName());
            assertThat(response.profileImage()).isEqualTo(testMember.getProfileImage());
            assertThat(response.createdAt()).isEqualTo(testMember.getCreatedAt());

            verify(memberReader).findById(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 조회 시 예외가 발생한다")
        void getProfile_WithNonExistentMemberId_ShouldThrowException() {
            // Given
            Long memberId = 999L;
            when(memberReader.findById(memberId))
                    .thenThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> profileService.getProfile(memberId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

            verify(memberReader).findById(memberId);
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfileTests {

        @Test
        @DisplayName("이름만 수정에 성공한다")
        void updateProfile_WithNameOnly_ShouldUpdateName() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createUpdateNameRequest();
            Member updatedMember = Member.builder()
                    .id(memberId)
                    .email(testMember.getEmail())
                    .name(request.name())
                    .profileImage(testMember.getProfileImage())
                    .provider(testMember.getProvider())
                    .providerId(testMember.getProviderId())
                    .isActive(testMember.getIsActive())
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(updatedMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.profileImage()).isEqualTo(testMember.getProfileImage());

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("프로필 이미지만 수정에 성공한다")
        void updateProfile_WithProfileImageOnly_ShouldUpdateProfileImage() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createUpdateImageRequest();
            Member updatedMember = Member.builder()
                    .id(memberId)
                    .email(testMember.getEmail())
                    .name(testMember.getName())
                    .profileImage(request.profileImage())
                    .provider(testMember.getProvider())
                    .providerId(testMember.getProviderId())
                    .isActive(testMember.getIsActive())
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(updatedMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(testMember.getName());
            assertThat(response.profileImage()).isEqualTo(request.profileImage());

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("이름과 프로필 이미지 모두 수정에 성공한다")
        void updateProfile_WithBothFields_ShouldUpdateBoth() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createUpdateBothRequest();
            Member updatedMember = Member.builder()
                    .id(memberId)
                    .email(testMember.getEmail())
                    .name(request.name())
                    .profileImage(request.profileImage())
                    .provider(testMember.getProvider())
                    .providerId(testMember.getProviderId())
                    .isActive(testMember.getIsActive())
                    .build();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(updatedMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.profileImage()).isEqualTo(request.profileImage());

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("빈 문자열로 프로필 이미지 삭제에 성공한다")
        void updateProfile_WithEmptyProfileImage_ShouldClearProfileImage() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createClearImageRequest();
            Member memberWithImage = Member.builder()
                    .id(memberId)
                    .email(testMember.getEmail())
                    .name(testMember.getName())
                    .profileImage("https://example.com/old-profile.jpg")
                    .provider(testMember.getProvider())
                    .providerId(testMember.getProviderId())
                    .isActive(testMember.getIsActive())
                    .build();
            Member updatedMember = Member.builder()
                    .id(memberId)
                    .email(testMember.getEmail())
                    .name(testMember.getName())
                    .profileImage(null)
                    .provider(testMember.getProvider())
                    .providerId(testMember.getProviderId())
                    .isActive(testMember.getIsActive())
                    .build();

            when(memberReader.findById(memberId)).thenReturn(memberWithImage);
            when(memberWriter.save(any(Member.class))).thenReturn(updatedMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.profileImage()).isNull();

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(memberWithImage);
        }

        @Test
        @DisplayName("빈 문자열 이름은 수정하지 않는다")
        void updateProfile_WithBlankName_ShouldNotUpdateName() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createBlankNameRequest();
            String originalName = testMember.getName();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(testMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(originalName);

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("모든 필드가 null이면 수정하지 않는다")
        void updateProfile_WithAllNullFields_ShouldNotUpdateAnything() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createAllNullRequest();
            String originalName = testMember.getName();
            String originalImage = testMember.getProfileImage();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(testMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(originalName);
            assertThat(response.profileImage()).isEqualTo(originalImage);

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("빈 문자열 이름도 수정하지 않는다")
        void updateProfile_WithEmptyName_ShouldNotUpdateName() {
            // Given
            Long memberId = 1L;
            UpdateProfileRequest request = ProfileFixture.createEmptyNameRequest();
            String originalName = testMember.getName();

            when(memberReader.findById(memberId)).thenReturn(testMember);
            when(memberWriter.save(any(Member.class))).thenReturn(testMember);

            // When
            ProfileResponse response = profileService.updateProfile(memberId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(originalName);

            verify(memberReader).findById(memberId);
            verify(memberWriter).save(testMember);
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 수정 시 예외가 발생한다")
        void updateProfile_WithNonExistentMemberId_ShouldThrowException() {
            // Given
            Long memberId = 999L;
            UpdateProfileRequest request = ProfileFixture.createUpdateNameRequest();

            when(memberReader.findById(memberId))
                    .thenThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> profileService.updateProfile(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

            verify(memberReader).findById(memberId);
            verify(memberWriter, never()).save(any());
        }
    }
}
