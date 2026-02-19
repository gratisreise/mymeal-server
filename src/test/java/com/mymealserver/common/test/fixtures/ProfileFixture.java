package com.mymealserver.common.test.fixtures;

import com.mymealserver.api.profile.dto.request.UpdateProfileRequest;

/**
 * Test fixture for Profile-related DTOs
 * Provides reusable UpdateProfileRequest instances for testing
 */
public class ProfileFixture {

    /**
     * Creates a request with only name updated
     */
    public static UpdateProfileRequest createUpdateNameRequest() {
        return new UpdateProfileRequest("Updated Name", null);
    }

    /**
     * Creates a request with only profile image updated
     */
    public static UpdateProfileRequest createUpdateImageRequest() {
        return new UpdateProfileRequest(null, "https://example.com/new-profile.jpg");
    }

    /**
     * Creates a request with both name and profile image updated
     */
    public static UpdateProfileRequest createUpdateBothRequest() {
        return new UpdateProfileRequest(
                "Updated Name",
                "https://example.com/new-profile.jpg"
        );
    }

    /**
     * Creates a request to clear profile image (empty string)
     */
    public static UpdateProfileRequest createClearImageRequest() {
        return new UpdateProfileRequest(null, "");
    }

    /**
     * Creates a request with blank name (should not update)
     */
    public static UpdateProfileRequest createBlankNameRequest() {
        return new UpdateProfileRequest("   ", null);
    }

    /**
     * Creates a request with all fields null (should not update anything)
     */
    public static UpdateProfileRequest createAllNullRequest() {
        return new UpdateProfileRequest(null, null);
    }

    /**
     * Creates a request with empty string name (should not update)
     */
    public static UpdateProfileRequest createEmptyNameRequest() {
        return new UpdateProfileRequest("", null);
    }

    /**
     * Creates a request with custom values
     */
    public static UpdateProfileRequest createCustomRequest(String name, String profileImage) {
        return new UpdateProfileRequest(name, profileImage);
    }
}
