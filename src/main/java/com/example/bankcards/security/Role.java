package com.example.bankcards.security;


import com.example.bankcards.entity.UserEntity;

/**
 * Enumeration of application-level security roles.
 * <p>
 * Defines the standard authorities recognized by Spring Security for
 * role-based access control throughout the system.
 * <p>
 * These roles are typically assigned to users via {@link UserEntity#getRoles()}
 * and used in authorization annotations such as {@code @PreAuthorize}.
 *
 * <p><b>Available roles:</b>
 * <ul>
 *   <li>{@link #ROLE_USER} — Standard authenticated user with limited privileges</li>
 *   <li>{@link #ROLE_ADMIN} — Administrator with full system access</li>
 * </ul>
 *
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see UserEntity
 * @see SecurityUtils
 */
public enum Role {

    /** Standard authenticated user with limited privileges. */
    ROLE_USER,

    /** Administrator with full access to user and card management operations. */
    ROLE_ADMIN
}
