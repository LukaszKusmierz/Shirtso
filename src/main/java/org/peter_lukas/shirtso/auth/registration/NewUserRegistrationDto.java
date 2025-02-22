package org.peter_lukas.shirtso.auth.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserRegistrationDto(

        @NotBlank(message = "Username can not be empty")
//        TODO unique email validation
        String username,

        @Email(message = "Login must be valid email")
        String email,

        @Size(min = 12, message = "Password should consist of 12 chars at least")
        String password
) {
}
