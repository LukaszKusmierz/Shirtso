package org.peter_lukas.shirtso.config;

import org.mockito.Mockito;
import org.peter_lukas.shirtso.notification.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockEmailConfig {

    @Bean
    @Primary
    public EmailService emailService() {
        // Create a mock that bypasses all the property injection issues
        return Mockito.mock(EmailService.class);
    }
}
