package com.madhura;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt-secret}")
    private String secret;
    private SecretKey key;

    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long.");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String create(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("role", "admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 12L * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public boolean isAdmin(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return "admin".equals(c.get("role", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}
