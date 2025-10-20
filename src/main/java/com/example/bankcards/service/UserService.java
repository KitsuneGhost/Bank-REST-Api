package com.example.bankcards.service;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.entity.CardEntity;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        security.requireAdmin();
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        security.requireSelfOrAdmin(id);
        UserEntity u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserMapper.toResponse(u);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        return UserMapper.toResponse(security.requireCurrentUser());
    }

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

    @Transactional
    public void deleteUser(Long id) {
        if (!security.isAdmin()) throw new AccessDeniedException("Access denied");
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

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
        String key = auth.getName(); // whatever you put in JWT subject
        return userRepository.findByUsername(key)
                .or(() -> userRepository.findByEmail(key))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
