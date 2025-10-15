package org.peter_lukas.shirtso.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.peter_lukas.shirtso.auth.config.AuthConfigProperties;
import org.peter_lukas.shirtso.utils.DateAdapter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
public class JWTTokenService {

    private final AuthConfigProperties authConfigProperties;
    private final DateAdapter dateAdapter;

    public JWTTokenService(AuthConfigProperties authConfigProperties, DateAdapter dateAdapter) {
        this.authConfigProperties = authConfigProperties;
        this.dateAdapter = dateAdapter;
    }

    public String createToken(String username, LocalDateTime passwordChangedAt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(authConfigProperties.validity());

        var jwtBuilder = Jwts.builder()
                .subject(username)
                .issuedAt(dateAdapter.convertToDate(now))
                .expiration(dateAdapter.convertToDate(expiration));

        if (passwordChangedAt != null) {
            Date passwordChangedDate = dateAdapter.convertToDate(passwordChangedAt);
            jwtBuilder.claim("password_changed_at", passwordChangedDate.getTime());
        }

        return jwtBuilder.signWith(getKey()).compact();
    }

    public boolean validateToken(String jwtToken, String springUserName, LocalDateTime userPasswordChangedAt) {
        log.debug("Validating token for user: {}", springUserName);
        String jwtUserName = getUserNameFromToken(jwtToken);
        Date jwtPasswordChangedAt = getPasswordChangedAtFromToken(jwtToken);
        Date expirationDate = getExpirationFromToken(jwtToken);
        boolean isExpired = getExpirationFromToken(jwtToken).before(new Date());

        log.debug("Token username: {}, Expected username: {}", jwtUserName, springUserName);
        log.debug("Token expiration: {}, Is expired: {}", expirationDate, isExpired);
        log.debug("Token password_changed_at: {}, User password_changed_at: {}", jwtPasswordChangedAt, userPasswordChangedAt);

        if (jwtPasswordChangedAt != null && userPasswordChangedAt != null) {
            LocalDateTime tokenPasswordTime = jwtPasswordChangedAt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            log.warn("=== TIMESTAMP COMPARISON DEBUG ===");
            log.warn("Token password_changed_at (from JWT): {}", tokenPasswordTime);
            log.warn("User password_changed_at (from DB):  {}", userPasswordChangedAt);
            log.warn("Token timestamp millis: {}", jwtPasswordChangedAt.getTime());
            log.warn("User timestamp millis:  {}", java.sql.Timestamp.valueOf(userPasswordChangedAt).getTime());
            log.warn("Difference in millis: {}",
                    java.sql.Timestamp.valueOf(userPasswordChangedAt).getTime() - jwtPasswordChangedAt.getTime());

            // Truncate to seconds to avoid precision issues
            LocalDateTime tokenPasswordTimeSeconds = tokenPasswordTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
            LocalDateTime userPasswordTimeSeconds = userPasswordChangedAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

            log.warn("Token time (truncated to seconds): {}", tokenPasswordTimeSeconds);
            log.warn("User time (truncated to seconds):  {}", userPasswordTimeSeconds);
            log.warn("User time is after token time: {}", userPasswordTimeSeconds.isAfter(tokenPasswordTimeSeconds));
            log.warn("=== END DEBUG ===");


            if (userPasswordTimeSeconds.isAfter(tokenPasswordTime)) {
                log.warn("Password changed after token was issued. Token invalid.");
                return false;
            }
        }
        boolean usernameMatches = jwtUserName.equals(springUserName);
        boolean isValid = !isExpired && usernameMatches;

        log.debug("Username matches: {}, Token valid: {}", usernameMatches, isValid);

        return isValid;
    }

    public String getUserNameFromToken(String jwtToken) {
        return getClaims(jwtToken).getSubject();
    }

    public Date getPasswordChangedAtFromToken(String jwtToken) {
        try {
            Claims claims = getClaims(jwtToken);
            Object passwordChangedAtObj = claims.get("password_changed_at");

            if (passwordChangedAtObj == null) {
                return null;
            }

            if (passwordChangedAtObj instanceof Number) {
                return new Date(((Number) passwordChangedAtObj).longValue());
            }

            if (passwordChangedAtObj instanceof String) {
                log.warn("Token contains old string-format password_changed_at, invalidating token");
                return null;
            }

            log.warn("Unexpected type for password_changed_at: {}", passwordChangedAtObj.getClass());
            return null;

        } catch (Exception e) {
            log.warn("Failed to parse password_changed_at from token", e);
            return null;
        }
    }

    public Date getExpirationFromToken(String jwtToken) {
        return getClaims(jwtToken).getExpiration();
    }

    private Claims getClaims(String jwtToken) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(authConfigProperties.secret().getBytes());
    }
}
