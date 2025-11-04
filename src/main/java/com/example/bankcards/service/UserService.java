package com.example.bankcards.service;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.Role;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.util.mapper.UserMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


/**
 * Service layer responsible for managing {@link UserEntity} operations,
 * including user creation, retrieval, updates, and deletion.
 * <p>
 * Provides both self-service user operations and administrative actions,
 * enforcing role-based access control via {@link SecurityUtils}.
 * <p>
 * Passwords are securely encoded using {@link PasswordEncoder},
 * and role assignments are handled automatically during creation.
 *
 * @see UserRepository
 * @see UserMapper
 * @see SecurityUtils
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils security;

    public UserService(UserRepository userRepository, CardRepository cardRepository,
                       PasswordEncoder passwordEncoder, SecurityUtils security) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.security = security;
    }

    /**
     * Retrieves all registered users in the system.
     * <p>
     * Only accessible by administrators.
     *
     * @return list of {@link UserResponseDTO} representing all users
     * @throws AccessDeniedException if the caller is not an admin
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        security.requireAdmin();
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves a user by ID.
     * <p>
     * Accessible to the user themselves or administrators.
     *
     * @param id ID of the user to retrieve
     * @return {@link UserResponseDTO} for the specified user
     * @throws IllegalArgumentException if the user does not exist
     * @throws AccessDeniedException    if the caller is unauthorized
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        security.requireSelfOrAdmin(id);
        UserEntity u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserMapper.toResponse(u);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return {@link UserResponseDTO} representing the authenticated user
     * @throws ResponseStatusException if authentication is missing or invalid
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        return UserMapper.toResponse(security.requireCurrentUser());
    }

    /**
     * Creates a new user account.
     * <p>
     * Accessible only to administrators. The user's password is securely encoded,
     * and the default role {@code ROLE_USER} is assigned automatically.
     *
     * @param dto request payload containing user creation data
     * @return {@link UserResponseDTO} of the newly created user
     * @throws AccessDeniedException if the caller is not an admin
     */
    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO dto) {
        if (!security.isAdmin()) throw new AccessDeniedException("Access denied");
        UserEntity u = UserMapper.toEntity(dto);
        u.setPassword(passwordEncoder.encode(dto.password()));
        // default role
        u.setRoles(java.util.Set.of(Role.ROLE_USER));
        userRepository.save(u);
        return UserMapper.toResponse(u);
    }

    /**
     * Updates the current user's profile information.
     * <p>
     * The user may update their own details and password.
     * Password changes are re-encoded using {@link PasswordEncoder}.
     *
     * @param dto update request containing new user data
     * @return updated {@link UserResponseDTO}
     */
    @Transactional
    public UserResponseDTO updateMe(UserUpdateRequestDTO dto) {
        var me = security.requireCurrentUser();
        UserMapper.applyUserUpdate(dto, me);
        if (dto.password() != null) {
            me.setPassword(passwordEncoder.encode(dto.password()));
        }
        userRepository.save(me);
        return UserMapper.toResponse(me);
    }

    /**
     * Updates another user's profile and credentials.
     * <p>
     * Admin-only operation. Allows role changes, status updates,
     * and password resets.
     *
     * @param id  ID of the user to update
     * @param dto admin-level update data
     * @return updated {@link UserResponseDTO}
     * @throws AccessDeniedException if the caller is not an admin
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public UserResponseDTO adminUpdateUser(Long id, AdminUserUpdateRequestDTO dto) {
        if (!security.isAdmin()) throw new AccessDeniedException("Access denied");
        UserEntity u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        UserMapper.applyAdminUpdate(dto, u);
        if (dto.password() != null) {
            u.setPassword(passwordEncoder.encode(dto.password()));
        }
        userRepository.save(u);
        return UserMapper.toResponse(u);
    }

    /**
     * Deletes a user account by its ID.
     * <p>
     * Only administrators are allowed to perform this action.
     *
     * @param id ID of the user to delete
     * @throws AccessDeniedException if the caller is not an admin
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!security.isAdmin()) throw new AccessDeniedException("Access denied");
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Retrieves the full {@link UserEntity} of the currently authenticated user
     * from the {@link SecurityContextHolder}.
     * <p>
     * Supports both {@link CustomUserDetails} principals (from JWT or session)
     * and fallback resolution by username or email.
     *
     * @return the authenticated {@link UserEntity}
     * @throws ResponseStatusException if the authentication is invalid or user not found
     */
    public UserEntity getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            Long id = cud.getId();
            return userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        }

        // Fallback path: auth.getName() might be username or email
        String key = auth.getName();
        return userRepository.findByUsername(key)
                .or(() -> userRepository.findByEmail(key))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
