package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.entity.UserEntity;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper() {}

    public static UserEntity toEntity(UserCreateRequestDTO dto) {
        UserEntity u = new UserEntity();
        u.setUsername(dto.username());
        u.setFullName(dto.fullName());
        u.setEmail(dto.email());
        // password encoded in service
        u.setPassword(dto.password());
        return u;
    }

    public static void applyUserUpdate(UserUpdateRequestDTO dto, UserEntity target) {
        if (dto == null || target == null) return;
        if (dto.username() != null && !Objects.equals(dto.username(), target.getUsername()))
            target.setUsername(dto.username());
        if (dto.fullName() != null && !Objects.equals(dto.fullName(), target.getFullName()))
            target.setFullName(dto.fullName());
        if (dto.email() != null && !Objects.equals(dto.email(), target.getEmail()))
            target.setEmail(dto.email());
        if (dto.password() != null) {
        }
    }

    public static void applyAdminUpdate(AdminUserUpdateRequestDTO dto, UserEntity target) {
        if (dto == null || target == null) return;
        if (dto.username() != null && !Objects.equals(dto.username(), target.getUsername()))
            target.setUsername(dto.username());
        if (dto.fullName() != null && !Objects.equals(dto.fullName(), target.getFullName()))
            target.setFullName(dto.fullName());
        if (dto.email() != null && !Objects.equals(dto.email(), target.getEmail()))
            target.setEmail(dto.email());
        if (dto.roles() != null)
            target.setRoles(dto.roles());
        if (dto.password() != null) {
            // handled in service (encode)
        }
    }

    public static UserResponseDTO toResponse(UserEntity u) {
        var roles = u.getRoles() == null ? Set.<String>of()
                : u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserResponseDTO(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), roles);
    }
}

