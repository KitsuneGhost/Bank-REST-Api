package com.example.bankcards.dto.auth;


/**
 * Data Transfer Object representing the response returned after a successful authentication.
 * <p>
 * Contains the generated JSON Web Token (JWT) and its expiration time in milliseconds.
 * <p>
 * Typically returned by the {@code POST /auth/login} endpoint after verifying user credentials.
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "expiresInMs": 86400000
 * }
 * </pre>
 *
 * @param token        the signed JWT string that grants access to protected resources
 * @param expiresInMs  token lifetime in milliseconds
 *                     (usually matches {@link com.example.bankcards.security.JwtUtils#getExpirationMs()})
 *
 * @see com.example.bankcards.security.JwtUtils
 * @see LoginRequest
 */
public record LoginResponse(String token, long expiresInMs) {
}
