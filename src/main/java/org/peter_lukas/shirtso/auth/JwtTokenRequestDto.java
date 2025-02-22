package org.peter_lukas.shirtso.auth;

public record JwtTokenRequestDto(
        String username,
        String password
) {
}
