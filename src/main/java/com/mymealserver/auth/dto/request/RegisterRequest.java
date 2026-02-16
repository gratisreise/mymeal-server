package com.mymealserver.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(

        @NotBlank(message = "{validation.email.notblank}")
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotBlank(message = "{validation.password.notblank}")
        @Size(min = 8, message = "{validation.password.size.min}")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
                message = "{validation.password.pattern}")
        String password,

        @NotBlank(message = "{validation.name.notblank}")
        @Size(max = 50, message = "{validation.name.size.max}")
        String name,

        String fcmToken

) {
}
