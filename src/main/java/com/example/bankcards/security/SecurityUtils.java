package com.example.bankcards.security;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Return current Authentication or null */
    public Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /** True if current user has ROLE_ADMIN */
    public boolean isAdmin() {
        var a = auth();
        if (a == null || a.getAuthorities() == null) return false;
        return a.getAuthorities().stream().anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }

    /** Username from SecurityContext (JWT subject) */
    public String currentUsername() {
        var a = auth();
        return a == null ? null : a.getName();
    }

    /** Optional<UserEntity> for the current user (loads from repo) */
    public Optional<UserEntity> currentUser() {
        String username = currentUsername();
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }

    /** Current user id or null */
    public Long currentUserId() {
        return currentUser().map(UserEntity::getId).orElse(null);
    }

    /** Require authenticated UserEntity or throw 403 */
    public UserEntity requireCurrentUser() {
        return currentUser().orElseThrow(() -> new AccessDeniedException("No authenticated user"));
    }

    /** Ensure caller is admin, else 403 */
    public void requireAdmin() {
        if (!isAdmin()) throw new AccessDeniedException("Admin only");
    }

    /** Ensure caller is the same as target id or admin */
    public void requireSelfOrAdmin(Long userId) {
        if (userId == null) throw new AccessDeniedException("Forbidden");
        if (isAdmin()) return;
        Long me = currentUserId();
        if (me == null || !me.equals(userId)) throw new AccessDeniedException("Forbidden");
    }
}