package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponseDTO toResponse(UserEntity u) {
        Set<String> roleNames = u.getRoles() == null ? Set.of()
                : u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                roleNames
        );
    }
}

