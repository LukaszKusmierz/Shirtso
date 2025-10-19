package org.peter_lukas.shirtso.auth;

import org.peter_lukas.shirtso.auth.user.Role;

import java.util.Set;
import java.util.UUID;

public record RegisterUserDataDto(

        UUID userId,
        String userName,
        String email,
        Set<String> roles
) {
    public RegisterUserDataDto(UUID userId, String userName, String email) {
        this(userId, userName, email, null);
    }
}
