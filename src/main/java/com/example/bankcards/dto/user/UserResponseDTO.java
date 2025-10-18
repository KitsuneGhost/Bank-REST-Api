package com.example.bankcards.dto.user;

public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        String role
) {}