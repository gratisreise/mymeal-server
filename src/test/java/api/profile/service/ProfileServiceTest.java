package api.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;
import com.mymealserver.api.profile.dto.response.ProfileResponse;
import com.mymealserver.api.profile.service.ProfileService;
import com.mymealserver.common.enums.ProviderType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberReader;
import com.mymealserver.domain.member.MemberWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock private MemberReader memberReader;
  @Mock private MemberWriter memberWriter;

  @InjectMocks private ProfileService profileService;

  private static final Long MEMBER_ID = 1L;

  // ========================
  // getProfile
  // ========================

  @Test
  void getProfile_success() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", "https://img.com/profile.jpg");
    given(memberReader.findById(MEMBER_ID)).willReturn(member);

    // when
    ProfileResponse response = profileService.getProfile(MEMBER_ID);

    // then
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(MEMBER_ID);
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.name()).isEqualTo("홍길동");
    assertThat(response.profileImage()).isEqualTo("https://img.com/profile.jpg");
  }

  @Test
  void getProfile_fail_memberNotFound() {
    // given
    given(memberReader.findById(MEMBER_ID)).willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> profileService.getProfile(MEMBER_ID))
        .isInstanceOf(BusinessException.class);
  }

  // ========================
  // updateProfile
  // ========================

  @Test
  void updateProfile_success_updateName() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", null);
    UpdateProfileRequest request = new UpdateProfileRequest("이몽룡", null);

    given(memberReader.findById(MEMBER_ID)).willReturn(member);
    given(memberWriter.save(member)).willReturn(member);

    // when
    ProfileResponse response = profileService.updateProfile(MEMBER_ID, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("이몽룡");
    then(memberWriter).should().save(member);
  }

  @Test
  void updateProfile_success_updateProfileImage() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", null);
    UpdateProfileRequest request = new UpdateProfileRequest(null, "https://img.com/new.jpg");

    given(memberReader.findById(MEMBER_ID)).willReturn(member);
    given(memberWriter.save(member)).willReturn(member);

    // when
    ProfileResponse response = profileService.updateProfile(MEMBER_ID, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.profileImage()).isEqualTo("https://img.com/new.jpg");
    then(memberWriter).should().save(member);
  }

  @Test
  void updateProfile_success_updateBoth() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", "https://img.com/old.jpg");
    UpdateProfileRequest request = new UpdateProfileRequest("이몽룡", "https://img.com/new.jpg");

    given(memberReader.findById(MEMBER_ID)).willReturn(member);
    given(memberWriter.save(member)).willReturn(member);

    // when
    ProfileResponse response = profileService.updateProfile(MEMBER_ID, request);

    // then
    assertThat(response.name()).isEqualTo("이몽룡");
    assertThat(response.profileImage()).isEqualTo("https://img.com/new.jpg");
  }

  @Test
  void updateProfile_success_blankNameIgnored() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", null);
    UpdateProfileRequest request = new UpdateProfileRequest("   ", null);

    given(memberReader.findById(MEMBER_ID)).willReturn(member);
    given(memberWriter.save(member)).willReturn(member);

    // when
    ProfileResponse response = profileService.updateProfile(MEMBER_ID, request);

    // then
    assertThat(response.name()).isEqualTo("홍길동"); // 이름 변경되지 않음
  }

  @Test
  void updateProfile_success_nullFieldsNoChange() {
    // given
    Member member = createMember(MEMBER_ID, "test@example.com", "홍길동", "https://img.com/profile.jpg");
    UpdateProfileRequest request = new UpdateProfileRequest(null, null);

    given(memberReader.findById(MEMBER_ID)).willReturn(member);
    given(memberWriter.save(member)).willReturn(member);

    // when
    ProfileResponse response = profileService.updateProfile(MEMBER_ID, request);

    // then
    assertThat(response.name()).isEqualTo("홍길동");
    assertThat(response.profileImage()).isEqualTo("https://img.com/profile.jpg");
  }

  // --- Helper ---

  private Member createMember(Long id, String email, String name, String profileImage) {
    return Member.builder()
        .id(id)
        .email(email)
        .name(name)
        .profileImage(profileImage)
        .provider(ProviderType.EMAIL)
        .build();
  }
}
