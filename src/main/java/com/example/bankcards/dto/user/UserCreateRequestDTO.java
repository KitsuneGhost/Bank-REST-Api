package com.example.bankcards.dto.user;

import jakarta.validation.constraints.*;

public record UserCreateRequestDTO(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String fullName,
        @NotBlank @Email String email
) {}
