package org.peter_lukas.shirtso.auth.password;

import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.auth.password.dto.*;
import org.peter_lukas.shirtso.auth.password.validation.ExpiredTokenException;
import org.peter_lukas.shirtso.auth.password.validation.IncorrectPasswordException;
import org.peter_lukas.shirtso.auth.password.validation.InvalidTokenException;
import org.peter_lukas.shirtso.auth.password.validation.UsedTokenException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.notification.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.peter_lukas.shirtso.messages.Alerts.*;

@Slf4j
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;
    private final int tokenValidityHours;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.password-reset.token-validity-hours:24}") int tokenValidityHours) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.tokenValidityHours = tokenValidityHours;
    }

    @Transactional
    public PasswordResetResponseDto requestPasswordReset(RequestPasswordResetDto request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());

        if (userOpt.isEmpty()) {
            log.info("Password reset requested for non-existent email: {}", request.email());
            return new PasswordResetResponseDto(true,
                    "If an account exists with this email, you will receive a password reset link.");
        }

        User user = userOpt.get();
        tokenRepository.invalidateUserTokens(user.getUserId());
        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);

        PasswordResetToken resetToken = new PasswordResetToken(user, token, expiryDate);
        tokenRepository.save(resetToken);
        sendPasswordResetEmail(user, token);

        log.info("Password reset token generated for user: {}", user.getEmail());

        return new PasswordResetResponseDto(true,
                "If an account exists with this email, you will receive a password reset link.");
    }

    @Transactional
    public PasswordResetResponseDto resetPassword(ResetPasswordDto request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException(INVALID_RESET_TOKEN));

        if (!resetToken.isValid()) {
            if (resetToken.isExpired()) {
                throw new ExpiredTokenException(EXPIRED_RESET_TOKEN);
            }
            throw new UsedTokenException(TOKEN_ALREADY_USED);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        sendPasswordResetConfirmationEmail(user);

        log.info("Password successfully reset for user: {}", user.getEmail());

        return new PasswordResetResponseDto(true, "Password has been successfully reset");
    }

    @Transactional
    public ChangePasswordResponseDto changePassword(ChangePasswordDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Failed password change attempt for user: {}", user.getEmail());
            throw new IncorrectPasswordException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            return new ChangePasswordResponseDto(false,
                    "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        sendPasswordChangeConfirmationEmail(user);

        log.info("Password successfully changed for user: {}", user.getEmail());

        return new ChangePasswordResponseDto(true, "Password has been successfully changed");
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        return resetTokenOpt.isPresent() && resetTokenOpt.get().isValid();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void sendPasswordResetEmail(User user, String token) {
        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token);

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(user.getUserName()).append(",\n\n");
        emailBody.append("You have requested to reset your password for your Shirtso account.\n\n");
        emailBody.append("Please click the link below to reset your password:\n");
        emailBody.append(resetLink).append("\n\n");
        emailBody.append("This link will expire in ").append(tokenValidityHours).append(" hours.\n\n");
        emailBody.append("If you did not request this password reset, please ignore this email ");
        emailBody.append("and your password will remain unchanged.\n\n");
        emailBody.append("For security reasons, never share this link with anyone.\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("Shirtso Team");

        emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request - Shirtso",
                emailBody.toString()
        );
    }

    private void sendPasswordResetConfirmationEmail(User user) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(user.getUserName()).append(",\n\n");
        emailBody.append("This is to confirm that your password has been successfully reset.\n\n");
        emailBody.append("If you did not make this change, please contact our support team immediately.\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("Shirtso Team");

        emailService.sendEmail(
                user.getEmail(),
                "Password Reset Confirmation - Shirtso",
                emailBody.toString()
        );
    }

    private void sendPasswordChangeConfirmationEmail(User user) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(user.getUserName()).append(",\n\n");
        emailBody.append("This is to confirm that your password has been successfully changed.\n\n");
        emailBody.append("If you did not make this change, please contact our support team immediately ");
        emailBody.append("and consider resetting your password.\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("Shirtso Team");

        emailService.sendEmail(
                user.getEmail(),
                "Password Change Confirmation - Shirtso",
                emailBody.toString()
        );
    }
}
