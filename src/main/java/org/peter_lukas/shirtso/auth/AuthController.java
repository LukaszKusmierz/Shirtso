package org.peter_lukas.shirtso.auth;

import jakarta.validation.Valid;
import org.peter_lukas.shirtso.auth.jwt.JWTTokenService;
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

    public AuthController(AuthenticationManager authenticationManager, JWTTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public JwtTokenResponse login(@Valid @RequestBody JwtTokenRequest jwtTokenRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(
                jwtTokenRequest.username(), jwtTokenRequest.password()
        );

        authenticationManager.authenticate(authToken);

        return new JwtTokenResponse(jwtTokenService.createToken(jwtTokenRequest.username()));
    }
}
