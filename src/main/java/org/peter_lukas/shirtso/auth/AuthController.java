package org.peter_lukas.shirtso.auth;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.analytics.LogExecutionTime;
import org.peter_lukas.shirtso.auth.config.CachedUserDetailsService;
import org.peter_lukas.shirtso.auth.jwt.JWTTokenService;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenService jwtTokenService;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, JWTTokenService jwtTokenService,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.authService = authService;
    }

    @PostMapping("/login")
    @LogExecutionTime
    public JwtTokenResponseDto login(@Valid @RequestBody JwtTokenRequestDto jwtTokenRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(
                jwtTokenRequest.username(), jwtTokenRequest.password()
        );
        var authentication = authenticationManager.authenticate(authToken);
        CachedUserDetailsService.CustomUserDetails userDetails =
                (CachedUserDetailsService.CustomUserDetails) authentication.getPrincipal();

        return new JwtTokenResponseDto(
                jwtTokenService.createToken(
                        jwtTokenRequest.username(),
                        userDetails.getPasswordChangedAt()
                )
        );
    }

    @PostMapping("/register")
    @LogExecutionTime
    public RegisterUserDataDto registerUser(@Valid @RequestBody NewUserRegistrationDto registrationDto) {
        return authService.registerNewUser(registrationDto);
    }

    @GetMapping("/me")
    @LogExecutionTime
    public RegisterUserDataDto getCurrentUser() throws UserNotFoundException {
        return authService.getCurrentUser();
    }

    @PostMapping("/logout")
    @LogExecutionTime
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
