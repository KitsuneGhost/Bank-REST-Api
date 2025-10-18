package com.example.bankcards.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final Key key;
    private final long expirationMs = 24 * 60 * 60 * 1000; // 24h

    public JwtUtils() {
        String b64 = System.getenv().getOrDefault("JWT_SECRET_B64",
                "ZmFrZS1qd3QtdG9rZW4tZm9yLXRlc3Rpbmc="); // REPLACE in prod
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(b64));
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String generateToken(CustomUserDetails user) {
        long now = System.currentTimeMillis();
        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("uid", user.getId())
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}