package org.peter_lukas.shirtso.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.unsplash")
public class UnsplashConfigProperties {

    private String accessKey;
    private String secretKey;
    private String apiUrl;
}
