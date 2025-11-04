package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;


/**
 * Data Transfer Object representing a self-service user profile update request.
 * <p>
 * Used by endpoints such as {@code PUT /users/me} to allow authenticated users
 * to update their own account information. All fields are optional, enabling
 * partial updates. When provided, the password must contain at least 8 characters,
 * and the email must be valid.
 *
 * <p><b>Example JSON payload:</b>
 * <pre>
 * {
 *   "username": "newusername",
 *   "password": "newsecurepassword",
 *   "fullName": "Johnathan Doe",
 *   "email": "johnathan.doe@example.com"
 * }
 * </pre>
 *
 * @param username new username for the user (optional)
 * @param password new password (optional, must contain at least 8 characters)
 * @param fullName updated full name (optional)
 * @param email    updated email address (optional, must follow valid format)
 *
 * @see com.example.bankcards.entity.UserEntity
 * @see com.example.bankcards.service.UserService#updateMe(UserUpdateRequestDTO)
 * @see jakarta.validation.constraints.Email
 * @see jakarta.validation.constraints.Size
 */
public record UserUpdateRequestDTO(
        String username,
        @Size(min = 8) String password,
        String fullName,
        @Email String email
) {}