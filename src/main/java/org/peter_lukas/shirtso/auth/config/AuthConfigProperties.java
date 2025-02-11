package org.peter_lukas.shirtso.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt.token")
public record AuthConfigProperties(
        String secret,
        Duration validity //minutes
) {
}
