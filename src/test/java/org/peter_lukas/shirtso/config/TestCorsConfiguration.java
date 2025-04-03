package org.peter_lukas.shirtso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestCorsConfiguration {

    @Bean
    @Primary
    public CorsConfigProperties corsConfigProperties() {
        CorsConfigProperties props = new CorsConfigProperties();
        props.setAllowedOrigins("http://localhost:3000");
        props.setAllowedMethods("GET,POST,PUT,DELETE,OPTIONS");
        props.setAllowedHeaders("*");
        props.setAllowCredentials(true);
        props.setMaxAge(3600L);
        return props;
    }
}
