package com.example.bankcards.util;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserService userService;

    public SecurityUtils(UserService userService) {
        this.userService = userService;
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName(); // you set subject = username in your JWT
    }

    public Long currentUserId() {
        String username = currentUsername();
        if (username == null) return null;
        UserEntity user = userService.findByUsername(username);
        return user.getId();
    }
}
