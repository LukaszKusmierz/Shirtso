package org.peter_lukas.shirtso.auth.password.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
