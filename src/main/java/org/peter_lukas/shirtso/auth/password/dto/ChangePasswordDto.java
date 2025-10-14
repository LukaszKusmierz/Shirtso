package org.peter_lukas.shirtso.auth.password.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank(message = "Current password required")
        String currentPassword,

        @NotBlank(message = "New password required")
        @Size(min = 12, message = "New password must be at least 12 characters long")
        String newPassword
) {
}
