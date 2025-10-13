package org.peter_lukas.shirtso.auth.password;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordResetTokenCleanupScheduler {

    private final PasswordResetService passwordResetService;

    public PasswordResetTokenCleanupScheduler(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {

        log.info("Starting scheduled cleanup of expired password reset tokens");

        passwordResetService.cleanupExpiredTokens();
    }
}
