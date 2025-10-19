package com.example.bankcards.dto.user;

import com.example.bankcards.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AdminUserUpdateRequestDTO(
        String username,
        @Size(min = 8) String password,
        String fullName,
        @Email String email,
        Set<Role> roles
) {}
