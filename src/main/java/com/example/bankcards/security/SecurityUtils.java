package com.example.bankcards.security;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * Utility component providing helper methods for working with the Spring Security context.
 * <p>
 * This class centralizes access to the current authentication information, user identity,
 * and role-based authorization checks. It simplifies retrieving the authenticated user
 * and verifying administrative privileges or ownership of resources.
 * <p>
 * All methods rely on {@link org.springframework.security.core.context.SecurityContextHolder}
 * to obtain the current {@link org.springframework.security.core.Authentication}.
 *
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @see org.springframework.security.core.Authentication
 * @see org.springframework.security.access.AccessDeniedException
 */
@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Retrieves the current {@link Authentication} object from the
     * {@link org.springframework.security.core.context.SecurityContextHolder}.
     *
     * @return current {@link Authentication}, or {@code null} if not authenticated
     */
    public Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    /**
     * Determines whether the currently authenticated user has the {@code ROLE_ADMIN} authority.
     *
     * @return {@code true} if the user has administrative privileges, {@code false} otherwise
     */
    public boolean isAdmin() {
        var a = auth();
        if (a == null || a.getAuthorities() == null) return false;
        return a.getAuthorities().stream().anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }


    /**
     * Retrieves the username (JWT subject) of the currently authenticated user.
     *
     * @return username of the current user, or {@code null} if unauthenticated
     */
    public String currentUsername() {
        var a = auth();
        return a == null ? null : a.getName();
    }


    /**
     * Loads the {@link UserEntity} corresponding to the currently authenticated user.
     *
     * @return optional containing the current user entity if authenticated and found in the repository;
     *         otherwise an empty optional
     */
    public Optional<UserEntity> currentUser() {
        String username = currentUsername();
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }


    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return current user ID, or {@code null} if unauthenticated or not found
     */
    public Long currentUserId() {
        return currentUser().map(UserEntity::getId).orElse(null);
    }


    /**
     * Retrieves the authenticated {@link UserEntity} or throws an exception if no user is authenticated.
     *
     * @return authenticated {@link UserEntity}
     * @throws AccessDeniedException if no authenticated user is present
     */
    public UserEntity requireCurrentUser() {
        return currentUser().orElseThrow(() -> new AccessDeniedException("No authenticated user"));
    }


    /**
     * Ensures that the current user has administrative privileges.
     *
     * @throws AccessDeniedException if the current user is not an administrator
     */
    public void requireAdmin() {
        if (!isAdmin()) throw new AccessDeniedException("Admin only");
    }


    /**
     * Ensures that the current user is either the specified user or has administrative privileges.
     * <p>
     * This method is typically used in service-layer access checks
     * to enforce resource ownership or admin override permissions.
     *
     * @param userId ID of the target user
     * @throws AccessDeniedException if the current user is neither admin nor the specified user
     */
    public void requireSelfOrAdmin(Long userId) {
        if (userId == null) throw new AccessDeniedException("Forbidden");
        if (isAdmin()) return;
        Long me = currentUserId();
        if (me == null || !me.equals(userId)) throw new AccessDeniedException("Forbidden");
    }
}