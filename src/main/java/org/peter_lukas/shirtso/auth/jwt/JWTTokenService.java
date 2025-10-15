package org.peter_lukas.shirtso.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.peter_lukas.shirtso.auth.config.AuthConfigProperties;
import org.peter_lukas.shirtso.utils.DateAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


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
            jwtBuilder.claim("password_changed_at", dateAdapter.convertToDate(passwordChangedAt));
        }

        return jwtBuilder.signWith(getKey()).compact();
    }

    public boolean validateToken(String jwtToken, String springUserName, LocalDateTime userPasswordChangedAt) {
        String jwtUserName = getUserNameFromToken(jwtToken);
        Date jwtPasswordChangedAt = getPasswordChangedAtFromToken(jwtToken);
        boolean isExpired = getExpirationFromToken(jwtToken).before(new Date());

        if (jwtPasswordChangedAt != null && userPasswordChangedAt != null) {
            LocalDateTime tokenPasswordTime = jwtPasswordChangedAt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            if (userPasswordChangedAt.isAfter(tokenPasswordTime)) {
                return false;
            }
        }
        return !isExpired && jwtUserName.equals(springUserName);
    }

    public String getUserNameFromToken(String jwtToken) {
        return getClaims(jwtToken).getSubject();
    }

    public Date getPasswordChangedAtFromToken(String jwtToken) {
        return getClaims(jwtToken).get("password_changed_at", Date.class);
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
