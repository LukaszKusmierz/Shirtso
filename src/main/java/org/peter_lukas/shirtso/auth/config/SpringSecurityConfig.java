package org.peter_lukas.shirtso.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.peter_lukas.shirtso.auth.jwt.JWTReqFilter;
import org.peter_lukas.shirtso.auth.jwt.JWTTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(AuthConfigProperties.class)
@EnableMethodSecurity(jsr250Enabled = true)
@SecurityScheme(
        name = "jwtauth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@OpenAPIDefinition(security = {@SecurityRequirement(name = "jwtauth")})
public class SpringSecurityConfig {

    public static final String DEVELOPER_READ = "DEVELOPER_READ";
    public static final String DEVELOPER_WRITE = "DEVELOPER_WRITE";
    private static final String[] URL_WHITELIST = {"/api/register", "/api/login", "/swagger-ui/**", "/v3/api-docs/**", "/error"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint, JWTReqFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz.requestMatchers(URL_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user@mail.com")
                .password(passwordEncoder.encode("usersecret"))
                .roles(DEVELOPER_READ)
                .build();

        UserDetails admin = User.withUsername("admin@mail.com")
                .password(passwordEncoder.encode("adminsecret"))
                .roles(
                        DEVELOPER_READ, DEVELOPER_WRITE)
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JWTTokenService jwtTokenService(AuthConfigProperties authConfigProperties) {
        return new JWTTokenService(authConfigProperties);
    }

    @Bean
    public JWTReqFilter jwtFilter(UserDetailsService userDetailsService, JWTTokenService jwtTokenService) {
        return new JWTReqFilter(jwtTokenService, userDetailsService);
    }
}
