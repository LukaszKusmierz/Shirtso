package org.peter_lukas.shirtso.auth.password;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.auth.password.dto.PasswordResetResponseDto;
import org.peter_lukas.shirtso.auth.password.dto.RequestPasswordResetDto;
import org.peter_lukas.shirtso.auth.password.dto.ResetPasswordDto;
import org.peter_lukas.shirtso.auth.password.validation.ExpiredTokenException;
import org.peter_lukas.shirtso.auth.password.validation.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    @LogExecutionTime
    public ResponseEntity<PasswordResetResponseDto> requestPasswordReset(
            @Valid @RequestBody RequestPasswordResetDto request) {
        try {
            PasswordResetResponseDto response = passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            log.error("Error processing password reset request", e);

            return ResponseEntity.ok(new PasswordResetResponseDto(true,
                    "If an account exists with this email, you will receive a password reset link."));
        }
    }

    @PostMapping("/reset-password")
    @LogExecutionTime
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto request) {
        try {
            PasswordResetResponseDto response = passwordResetService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponseDto(false, e.getMessage()));
        } catch (ExpiredTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponseDto(false, e.getMessage()));
        } catch (Exception e) {

            log.error("Error resetting password", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordResetResponseDto(false, "An error occurred while resetting password"));
        }
    }

    @GetMapping("/validate-token/{token}")
    @LogExecutionTime
    public ResponseEntity<PasswordResetResponseDto> validateToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(new PasswordResetResponseDto(true, "Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetResponseDto(false, "Token is invalid or expired"));
        }
    }
}
