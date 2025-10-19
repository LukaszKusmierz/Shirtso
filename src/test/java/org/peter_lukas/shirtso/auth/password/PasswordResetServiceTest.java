package org.peter_lukas.shirtso.auth.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.auth.password.dto.*;
import org.peter_lukas.shirtso.auth.password.validation.ExpiredTokenException;
import org.peter_lukas.shirtso.auth.password.validation.IncorrectPasswordException;
import org.peter_lukas.shirtso.auth.password.validation.InvalidTokenException;
import org.peter_lukas.shirtso.auth.password.validation.UsedTokenException;
import org.peter_lukas.shirtso.auth.user.User;
import org.peter_lukas.shirtso.auth.user.UserRepository;
import org.peter_lukas.shirtso.notification.EmailService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private CacheManager cacheManager;

    private PasswordResetService passwordResetService;

    private User testUser;

    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testToken = createTestToken();

        passwordResetService = new PasswordResetService(
                tokenRepository, userRepository, passwordEncoder,
                emailService, cacheManager,
                "http://localhost:3000", 24
        );
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedOldPassword");
        return user;
    }

    private PasswordResetToken createTestToken() {
        PasswordResetToken token = new PasswordResetToken(
                testUser,
                "test-token-123",
                LocalDateTime.now().plusHours(24)
        );
        token.setTokenId(1);
        return token;
    }

    @Test
    void requestPasswordReset_WithValidEmail_SendsEmail() {
        // given
        RequestPasswordResetDto request = new RequestPasswordResetDto("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // when
        PasswordResetResponseDto result = passwordResetService.requestPasswordReset(request);

        // then
        assertThat(result.success()).isTrue();
        verify(tokenRepository).invalidateUserTokens(testUser.getUserId());
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendEmail(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void requestPasswordReset_WithNonExistentEmail_ReturnsSuccessMessage() {
        // given
        RequestPasswordResetDto request = new RequestPasswordResetDto("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when
        PasswordResetResponseDto result = passwordResetService.requestPasswordReset(request);

        // then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("If an account exists");
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void requestPasswordReset_InvalidatesOldTokens() {
        // given
        RequestPasswordResetDto request = new RequestPasswordResetDto("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // when
        passwordResetService.requestPasswordReset(request);

        // then
        verify(tokenRepository).invalidateUserTokens(testUser.getUserId());
    }

    @Test
    void resetPassword_WithValidToken_ResetsPassword() {
        // given
        ResetPasswordDto request = new ResetPasswordDto("test-token-123", "newPassword123");

        when(tokenRepository.findByToken("test-token-123")).thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(tokenRepository.save(testToken)).thenReturn(testToken);
        when(cacheManager.getCache("usersByEmail")).thenReturn(mock(Cache.class));

        // when
        PasswordResetResponseDto result = passwordResetService.resetPassword(request);

        // then
        assertThat(result.success()).isTrue();
        assertThat(testToken.isUsed()).isTrue();
        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode("newPassword123");
        verify(emailService).sendEmail(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void resetPassword_WithInvalidToken_ThrowsException() {
        // given
        ResetPasswordDto request = new ResetPasswordDto("invalid-token", "newPassword123");

        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_WithExpiredToken_ThrowsException() {
        // given
        testToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        ResetPasswordDto request = new ResetPasswordDto("test-token-123", "newPassword123");

        when(tokenRepository.findByToken("test-token-123")).thenReturn(Optional.of(testToken));

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(ExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_WithUsedToken_ThrowsException() {
        // given
        testToken.setUsed(true);
        ResetPasswordDto request = new ResetPasswordDto("test-token-123", "newPassword123");

        when(tokenRepository.findByToken("test-token-123")).thenReturn(Optional.of(testToken));

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(UsedTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WithCorrectCurrentPassword_ChangesPassword() {
        // given
        setupSecurityContext();
        ChangePasswordDto request = new ChangePasswordDto("oldPassword", "newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(cacheManager.getCache("usersByEmail")).thenReturn(mock(Cache.class));

        // when
        ChangePasswordResponseDto result = passwordResetService.changePassword(request);

        // then
        assertThat(result.success()).isTrue();
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(emailService).sendEmail(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ThrowsException() {
        // given
        setupSecurityContext();
        ChangePasswordDto request = new ChangePasswordDto("wrongPassword", "newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> passwordResetService.changePassword(request))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WithSamePassword_ReturnsFailure() {
        // given
        setupSecurityContext();
        ChangePasswordDto request = new ChangePasswordDto("oldPassword", "oldPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);

        // when
        ChangePasswordResponseDto result = passwordResetService.changePassword(request);

        // then
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("must be different");
        verify(userRepository, never()).save(any());
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // given
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(testToken));

        // when
        boolean result = passwordResetService.validateToken("valid-token");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        // given
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // when
        boolean result = passwordResetService.validateToken("invalid-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // given
        testToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(testToken));

        // when
        boolean result = passwordResetService.validateToken("expired-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void cleanupExpiredTokens_DeletesExpiredTokens() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        passwordResetService.cleanupExpiredTokens();

        // then
        verify(tokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void resetPassword_ClearsUserCache() {
        // given
        ResetPasswordDto request = new ResetPasswordDto("test-token-123", "newPassword123");
        Cache mockCache = mock(Cache.class);

        when(tokenRepository.findByToken("test-token-123")).thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(tokenRepository.save(testToken)).thenReturn(testToken);
        when(cacheManager.getCache("usersByEmail")).thenReturn(mockCache);

        // when
        passwordResetService.resetPassword(request);

        // then
        verify(mockCache).evict(testUser.getEmail());
        verify(mockCache).evict(testUser.getUserName());
    }

    private void setupSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), "password"
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}