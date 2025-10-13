package org.peter_lukas.shirtso.auth.password.dto;

public record PasswordResetResponseDto(
        boolean success,
        String message
) {
}
