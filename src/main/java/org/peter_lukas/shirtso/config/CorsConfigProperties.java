package org.peter_lukas.shirtso.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class CorsConfigProperties {

    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private boolean allowCredentials;
    private long maxAge;

    public List<String> getAllowedOriginsList() {
        return List.of(allowedOrigins.split(","));
    }

    public List<String> getAllowedMethodsList() {
        return List.of(allowedMethods.split(","));
    }

    public List<String> getAllowedHeadersList() {
        return List.of(allowedHeaders.split(","));
    }
}
