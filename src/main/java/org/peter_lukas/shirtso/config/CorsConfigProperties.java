package org.peter_lukas.shirtso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsConfigProperties(
        String allwedOrigins,
        String allowedMethods,
        String allowedHeaders,
        boolean allowedCredentials,
        long maxAge
) {
    public List<String> getAllowedOriginsList() {
        return List.of(allowedHeaders.split(","));
    }

    public List<String> getAllowedMethodsList() {
        return List.of(allowedMethods.split(","));
    }

    public List<String> getAllowedHeadersList() {
        return List.of(allowedHeaders.split(","));
    }
}
