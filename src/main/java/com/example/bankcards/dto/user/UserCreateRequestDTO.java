package com.example.bankcards.dto.user;

import jakarta.validation.constraints.*;

public record UserCreateRequestDTO(
        @NotBlank @Size(min=2, max=100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min=8, max=100) String password,
        @NotBlank @Pattern(regexp="ADMIN|USER") String role
) {}
