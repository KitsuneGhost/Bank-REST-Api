package com.example.bankcards.dto.user;

import jakarta.validation.constraints.*;

public record UserRequestDTO(
        @NotBlank @Size(min=2, max=100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min=8, max=100) String password
) {}
