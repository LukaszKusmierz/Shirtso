package org.peter_lukas.shirtso.auth.password.dto;

public record ChangePasswordResponseDto(
        boolean success,
        String message
) {
}
