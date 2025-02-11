package org.peter_lukas.shirtso.auth;

public record JwtTokenRequest(
        String username,
        String password
) {
}
