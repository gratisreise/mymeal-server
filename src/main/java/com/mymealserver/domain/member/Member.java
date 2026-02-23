package com.mymealserver.domain.member;

import com.mymealserver.domain.base.SoftDeletable;
import com.mymealserver.common.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderType provider;

    @Column(length = 255)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column
    private LocalDateTime lastLoginAt;

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void updateProfileImage(String profileImage) {
        if (profileImage != null) {
            this.profileImage = profileImage.isBlank() ? null : profileImage;
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }
}
