package com.example.bankcards.dto.user;

import java.util.Set;


/**
 * Data Transfer Object representing a public view of a {@link com.example.bankcards.entity.UserEntity}.
 * <p>
 * Used in API responses to expose basic user information while omitting
 * sensitive fields such as passwords or internal identifiers.
 * <p>
 * Typically returned by endpoints such as:
 * <ul>
 *   <li>{@code GET /users}</li>
 *   <li>{@code GET /users/{id}}</li>
 *   <li>{@code GET /users/me}</li>
 * </ul>
 *
 * <p><b>Example JSON response:</b>
 * <pre>
 * {
 *   "id": 5,
 *   "username": "johndoe",
 *   "fullName": "John Doe",
 *   "email": "john.doe@example.com",
 *   "roles": ["ROLE_USER"]
 * }
 * </pre>
 *
 * @param id        unique identifier of the user
 * @param username  user's username
 * @param fullName  user's full name
 * @param email     user's email address
 * @param roles     set of roles assigned to the user (e.g., {@code ROLE_USER}, {@code ROLE_ADMIN})
 *
 * @see com.example.bankcards.entity.UserEntity
 * @see com.example.bankcards.service.UserService
 * @see com.example.bankcards.security.Role
 */
public record UserResponseDTO(
        Long id,
        String username,
        String fullName,
        String email,
        Set<String> roles
) {}