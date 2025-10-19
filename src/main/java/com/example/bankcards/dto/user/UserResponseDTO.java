package com.example.bankcards.dto.user;

import java.util.Set;

public record UserResponseDTO(
        Long id,
        String username,
        String fullName,
        String email,
        Set<String> roles
) {}