package org.peter_lukas.shirtso.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.peter_lukas.shirtso.auth.config.CachedUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class JWTReqFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";

    private final JWTTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JWTReqFilter(JWTTokenService jwtTokenService, UserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("JWT Filter processing request to: {}", request.getRequestURI());
        String authHeader = request.getHeader(AUTHORIZATION);
        log.debug("Authorization header: {}", authHeader != null ? "present" : "missing");
        if (authHeader != null && authHeader.startsWith(BEARER)) {
            String jwtToken = authHeader.substring(BEARER.length() + 1);
            log.debug("Extracted JWT token (first 20 chars): {}", jwtToken.substring(0, Math.min(20, jwtToken.length())));

            try {
                String userName = jwtTokenService.getUserNameFromToken(jwtToken);
                log.debug("Extracted username from token: {}", userName);
                authenticateUser(request, jwtToken, userName);
            } catch (IllegalArgumentException e) {
                log.warn("Parsing JWT failed", e);
            } catch (ExpiredJwtException ex) {
                log.warn("JWT {} expired", jwtToken);
            } catch (Exception e) {
                log.warn("Unexpected error processing JWT", e);
            }
        } else {
            log.debug("No valid JWT found in request");
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request, String jwtToken, String userName) {
        log.debug("Authenticating user: {}", userName);
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            LocalDateTime passwordChangedAt = null;

            if (userDetails instanceof CachedUserDetailsService.CustomUserDetails customUserDetails) {
                passwordChangedAt = customUserDetails.user().getPasswordChangedAt();
            }

            if (jwtTokenService.validateToken(jwtToken, userName, passwordChangedAt)) {
                log.debug("Token validated successfully for user: {}", userName);
                var springAuthToken = new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities()
                );

                springAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(springAuthToken);
                log.debug("SecurityContext set with authentication for user: {}", userName);
            } else {
                log.warn("Token validation failed for user: {}", userName);
            }
        } else {
            log.debug("Authentication skipped - userName: {}, existing auth: {}",
                    userName, SecurityContextHolder.getContext().getAuthentication());
        }
    }
}
