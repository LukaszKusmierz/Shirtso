package org.peter_lukas.shirtso.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@SecurityScheme(
        name = "jwtauth",
        type = SecuritySchemeTpe.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

public class SpringSecurityConfig {
}
