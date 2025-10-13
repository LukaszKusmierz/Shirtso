package org.peter_lukas.shirtso.auth.password.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 12, message = "Password must be at least 12 characters")
        String newPassword
) {
}
