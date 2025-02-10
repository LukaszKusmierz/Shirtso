package org.peter_lukas.shirtso.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.peter_lukas.shirtso.auth.config.AuthConfigProperties;
import org.peter_lukas.shirtso.utils.DateAdapter;
import org.peter_lukas.shirtso.utils.LocalDateTimeToDateAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;


public class JWTTokenService {

    private final AuthConfigProperties authConfigProperties;
    private final DateAdapter dateAdapter = new LocalDateTimeToDateAdapter();

    public JWTTokenService(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    public String createToken(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(authConfigProperties.validity());

        return Jwts.builder()
                .subject(username)
                .issuedAt(dateAdapter.convertToDate(now))
                .expiration(dateAdapter.convertToDate(expiration))
                .signWith(getKey())
                .compact();
    }

    public boolean validateToken(String jwtToken, String springUserName) {
        String jwtUserName = getUserNameFromToken(jwtToken);
        boolean isExpired = getExpirationFromToken(jwtToken).before(new Date());
        return !isExpired && jwtUserName.equals(springUserName);
    }

    public String getUserNameFromToken(String jwtToken) {
        return getClaims(jwtToken).getSubject();
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
