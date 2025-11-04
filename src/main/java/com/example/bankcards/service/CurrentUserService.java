package com.example.bankcards.service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


/**
 * Service component that resolves and returns the currently authenticated {@link UserEntity}.
 * <p>
 * This class provides a simplified, reusable way to fetch the authenticated user
 * directly from the {@link org.springframework.security.core.context.SecurityContextHolder}.
 * <p>
 * It is typically used by other service layers (such as {@code CardService})
 * to attach ownership information or enforce access control without duplicating
 * security context logic.
 *
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @see org.springframework.security.core.Authentication
 * @see UserRepository
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the {@link UserEntity} corresponding to the currently authenticated user.
     * <p>
     * The authentication is obtained from the {@link SecurityContextHolder}.
     * If no authentication is present or if the user cannot be found in the database,
     * an {@link IllegalStateException} is thrown.
     *
     * @return the {@link UserEntity} of the current authenticated user
     * @throws IllegalStateException if no authentication exists or user record is missing
     */
    public UserEntity requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }

        String username = auth.getName(); // same as CustomUserDetails.getUsername()
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));
    }
}
