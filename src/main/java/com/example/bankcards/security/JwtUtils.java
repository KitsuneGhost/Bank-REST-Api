package com.example.bankcards.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;


/**
 * Utility component for generating and validating JSON Web Tokens (JWT)
 * used in authentication and authorization within the application.
 * <p>
 * Encapsulates JWT creation, signature verification, and claim extraction logic
 * using the {@code io.jsonwebtoken} (JJWT) library.
 * <p>
 * Tokens are signed using the HMAC SHA-256 algorithm with a Base64-encoded secret
 * key provided via the {@code jwt.secret-b64} property in {@code application.yml}.
 * <p>
 * Each token includes:
 * <ul>
 *   <li>{@code sub} — the username (JWT subject)</li>
 *   <li>{@code uid} — the unique user ID</li>
 *   <li>{@code roles} — comma-separated list of granted authorities</li>
 * </ul>
 * Tokens are valid for 24 hours by default.
 *
 * @see io.jsonwebtoken.Jwts
 * @see io.jsonwebtoken.security.Keys
 * @see CustomUserDetails
 * @see org.springframework.security.core.GrantedAuthority
 */
@Component
public class JwtUtils {

    private final Key key;
    private final long expirationMs = 24 * 60 * 60 * 1000; // 24h

    public JwtUtils(@Value("${jwt.secret-b64}") String secretBase64) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
    }


    /**
     * Returns the configured JWT expiration duration in milliseconds.
     * <p>
     * Default: 24 hours (86,400,000 ms).
     *
     * @return token expiration duration in milliseconds
     */
    public long getExpirationMs() {
        return expirationMs;
    }


    /**
     * Generates a signed JWT token for the specified user.
     * <p>
     * The generated token includes:
     * <ul>
     *   <li>Subject — the user's username</li>
     *   <li>Claim {@code uid} — the user ID</li>
     *   <li>Claim {@code roles} — comma-separated list of granted authorities</li>
     *   <li>Issued and expiration timestamps</li>
     * </ul>
     *
     * @param user authenticated user details implementing {@link CustomUserDetails}
     * @return signed JWT token as a compact string
     */
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


    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return username contained in the token's {@code sub} claim
     * @throws io.jsonwebtoken.JwtException if the token is invalid or tampered
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }


    /**
     * Validates the structure, signature, and expiration of a JWT token.
     * <p>
     * Returns {@code true} if the token is correctly signed and not expired;
     * otherwise returns {@code false}.
     *
     * @param token JWT token to validate
     * @return {@code true} if valid, {@code false} if expired or malformed
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}