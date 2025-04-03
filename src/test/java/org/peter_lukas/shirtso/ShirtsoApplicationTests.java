package org.peter_lukas.shirtso;

import org.junit.jupiter.api.Test;
import org.peter_lukas.shirtso.config.CorsConfigProperties;
import org.peter_lukas.shirtso.config.MockEmailConfig;
import org.peter_lukas.shirtso.config.TestCorsConfiguration;
import org.peter_lukas.shirtso.config.WebConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({MockEmailConfig.class, TestCorsConfiguration.class})
@ActiveProfiles("test")
class ShirtsoApplicationTests {

    @Test
    void contextLoads() {
    }

}
