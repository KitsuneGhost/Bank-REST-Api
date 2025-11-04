package com.example.bankcards.dto.user;

import com.example.bankcards.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;


/**
 * Data Transfer Object representing an administrative request to update an existing user account.
 * <p>
 * Used by admin-only endpoints (e.g., {@code PUT /admin/users/{id}}) to modify user
 * credentials, personal information, or assigned roles.
 * <p>
 * All fields are optional, allowing partial updates. When provided, the password
 * must be at least 8 characters long, and the email must follow a valid email format.
 *
 * <p><b>Example JSON payload:</b>
 * <pre>
 * {
 *   "username": "newusername",
 *   "password": "newstrongpass",
 *   "fullName": "Johnathan Doe",
 *   "email": "johnathan.doe@example.com",
 *   "roles": ["ROLE_ADMIN", "ROLE_USER"]
 * }
 * </pre>
 *
 * @param username new username for the user (optional)
 * @param password new password (optional, must contain at least 8 characters)
 * @param fullName new full name for the user (optional)
 * @param email    new email address (optional, validated by {@link jakarta.validation.constraints.Email})
 * @param roles    new set of roles to assign to the user (optional)
 *
 * @see com.example.bankcards.entity.UserEntity
 * @see com.example.bankcards.service.UserService#adminUpdateUser(Long, AdminUserUpdateRequestDTO)
 * @see jakarta.validation.constraints.Email
 * @see jakarta.validation.constraints.Size
 */
public record AdminUserUpdateRequestDTO(
        String username,
        @Size(min = 8) String password,
        String fullName,
        @Email String email,
        Set<Role> roles
) {}
