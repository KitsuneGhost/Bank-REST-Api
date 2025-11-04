package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Data Transfer Object representing a user registration request payload.
 * <p>
 * Used by the {@code POST /auth/register} endpoint to create a new user account.
 * Includes basic validation annotations for input fields such as username,
 * email, full name, and password length.
 * <p>
 * Example JSON payload:
 * <pre>
 * {
 *   "username": "johndoe",
 *   "email": "john.doe@example.com",
 *   "fullName": "John Doe",
 *   "password": "strongpassword123"
 * }
 * </pre>
 *
 * @param username the unique username chosen by the user (required)
 * @param email    the user's email address, validated by {@link jakarta.validation.constraints.Email}
 * @param fullName the user's full name (required)
 * @param password the plaintext password to be encoded before persistence; must have at least 8 characters
 *
 * @see jakarta.validation.constraints.NotBlank
 * @see jakarta.validation.constraints.Email
 * @see jakarta.validation.constraints.Size
 * @see com.example.bankcards.service.UserService
 */
public record RegisterRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String fullName,
        @Size(min=8) String password
) {}
