package org.peter_lukas.shirtso.auth.registration;

import java.util.UUID;

public record RegisterUserDataDto(

        UUID user_id,
        String login
) {
}
