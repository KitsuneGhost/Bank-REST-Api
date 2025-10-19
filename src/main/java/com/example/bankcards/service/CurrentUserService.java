package com.example.bankcards.service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
