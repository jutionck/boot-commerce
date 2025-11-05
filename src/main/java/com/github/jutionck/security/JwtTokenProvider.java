package com.github.jutionck.security;

import com.github.jutionck.entity.User;
import com.github.jutionck.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.refresh_expiration}")
    private Long jwtRefreshToken;

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwtIssuer)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpiration))
                .claim("role", user.getRole().getRoleName())
                .signWith(getSigningKey())
                .compact();
    }

    // 2. Verify Token
    public boolean verifyToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Token has expired");
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid token: " + e.getMessage());
        }
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
