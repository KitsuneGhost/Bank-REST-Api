package com.example.bankcards.dto.auth;


/**
 * Data Transfer Object representing a login request payload.
 * <p>
 * Used by the authentication endpoint to capture user credentials
 * during the sign-in process.
 * <p>
 * Typically sent as JSON in a {@code POST /auth/login} request body.
 * <pre>
 * {
 *   "username": "johndoe",
 *   "password": "secret123"
 * }
 * </pre>
 *
 * @param username the username identifying the user attempting to log in
 * @param password the user's plaintext password (validated and encoded by the authentication layer)
 *
 * @see com.example.bankcards.security.JwtUtils
 */
public record LoginRequest(String username, String password) {}