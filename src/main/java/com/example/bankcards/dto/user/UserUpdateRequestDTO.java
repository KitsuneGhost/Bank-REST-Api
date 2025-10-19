package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        String username,
        @Size(min = 8) String password,
        String fullName,
        @Email String email
) {}