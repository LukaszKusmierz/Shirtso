package org.peter_lukas.shirtso.auth;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.auth.jwt.JWTTokenService;
import org.peter_lukas.shirtso.auth.registration.AuthService;
import org.peter_lukas.shirtso.auth.registration.NewUserRegistrationDto;
import org.peter_lukas.shirtso.auth.registration.RegisterUserDataDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public JwtTokenResponseDto login(@Valid @RequestBody JwtTokenRequestDto jwtTokenRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(
                jwtTokenRequest.username(), jwtTokenRequest.password()
        );

        authenticationManager.authenticate(authToken);

        return new JwtTokenResponseDto(jwtTokenService.createToken(jwtTokenRequest.username()));
    }

    @PostMapping("/register")
    public RegisterUserDataDto registerUser(@Valid @RequestBody NewUserRegistrationDto registrationDto) {
        return authService.registerNewUser(registrationDto);


    }
}
