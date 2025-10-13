package org.peter_lukas.shirtso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ShirtsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShirtsoApplication.class, args);
    }

}
