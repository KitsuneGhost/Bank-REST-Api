package com.example.bankcards.dto.user;

import jakarta.validation.constraints.*;


/**
 * Data Transfer Object representing a request to create a new user account.
 * <p>
 * Used by administrative endpoints (e.g., {@code POST /admin/users}) or during
 * registration processes to create a new {@link com.example.bankcards.entity.UserEntity}.
 * <p>
 * All fields are mandatory and validated for format and minimum length requirements.
 * The password must contain at least 8 characters and will be securely encoded
 * before being stored in the database.
 *
 * <p><b>Example JSON payload:</b>
 * <pre>
 * {
 *   "username": "johndoe",
 *   "password": "securePass123",
 *   "fullName": "John Doe",
 *   "email": "john.doe@example.com"
 * }
 * </pre>
 *
 * @param username unique username chosen by the user (required)
 * @param password plaintext password (required, minimum 8 characters; encoded before persistence)
 * @param fullName user's full name (required)
 * @param email    user's email address (required, must be valid)
 *
 * @see com.example.bankcards.entity.UserEntity
 * @see com.example.bankcards.service.UserService#createUser(UserCreateRequestDTO)
 * @see jakarta.validation.constraints.NotBlank
 * @see jakarta.validation.constraints.Email
 * @see jakarta.validation.constraints.Size
 */
public record UserCreateRequestDTO(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String fullName,
        @NotBlank @Email String email
) {}
