package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.entity.UserEntity;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utility class responsible for mapping between {@link UserEntity}
 * and its corresponding Data Transfer Objects (DTOs).
 * <p>
 * Provides static conversion and update methods used by service-layer
 * components to transform user data between persistence and API layers.
 * <p>
 * This mapper ensures consistent handling of user information and delegates
 * password encoding to the {@link com.example.bankcards.service.UserService}.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Convert {@link UserCreateRequestDTO} into a {@link UserEntity} for persistence.</li>
 *   <li>Apply user-initiated updates from {@link UserUpdateRequestDTO}.</li>
 *   <li>Apply admin-initiated updates from {@link AdminUserUpdateRequestDTO}.</li>
 *   <li>Convert {@link UserEntity} into a {@link UserResponseDTO} for API responses.</li>
 * </ul>
 *
 * @see com.example.bankcards.service.UserService
 * @see com.example.bankcards.dto.user.UserCreateRequestDTO
 * @see com.example.bankcards.dto.user.UserResponseDTO
 */
public final class UserMapper {

    /** Private constructor to prevent instantiation (utility class). */
    private UserMapper() {}


    /**
     * Converts a {@link UserCreateRequestDTO} into a new {@link UserEntity}.
     * <p>
     * The password is passed as-is and must be encoded later in the service layer
     * before the entity is persisted.
     *
     * @param dto the DTO containing new user information
     * @return a new {@link UserEntity} populated with the provided data
     */
    public static UserEntity toEntity(UserCreateRequestDTO dto) {
        UserEntity u = new UserEntity();
        u.setUsername(dto.username());
        u.setFullName(dto.fullName());
        u.setEmail(dto.email());
        // password encoded in service
        u.setPassword(dto.password());
        return u;
    }


    /**
     * Applies self-service updates from a {@link UserUpdateRequestDTO} to an existing {@link UserEntity}.
     * <p>
     * Updates only non-null and changed fields, leaving others unchanged.
     * Password updates are handled separately in the service layer (with encoding).
     *
     * @param dto    the update request containing user-provided fields
     * @param target the entity to be updated
     */
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


    /**
     * Applies admin-level updates from a {@link AdminUserUpdateRequestDTO} to an existing {@link UserEntity}.
     * <p>
     * Allows updating of roles and other sensitive attributes in addition to standard fields.
     * Password updates are handled separately in the service layer (with encoding).
     *
     * @param dto    the admin update request
     * @param target the entity to be updated
     */
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


    /**
     * Converts a {@link UserEntity} into a {@link UserResponseDTO} for API output.
     * <p>
     * The response includes non-sensitive user details such as ID, username, name,
     * email, and role names. Passwords are never exposed.
     *
     * @param u the user entity to convert
     * @return a {@link UserResponseDTO} representing the user for API responses
     */
    public static UserResponseDTO toResponse(UserEntity u) {
        var roles = u.getRoles() == null ? Set.<String>of()
                : u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserResponseDTO(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), roles);
    }
}

