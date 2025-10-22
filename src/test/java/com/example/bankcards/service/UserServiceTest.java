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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityUtils securityUtils;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, cardRepository, passwordEncoder, securityUtils);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllUsers_requiresAdminAndMapsResults() {
        doNothing().when(securityUtils).requireAdmin();
        UserEntity user = new UserEntity(1L, "jdoe", "John Doe", "pass", "john@example.com", new ArrayList<>());
        user.setRoles(Set.of(Role.ROLE_USER));
        given(userRepository.findAll()).willReturn(List.of(user));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("jdoe");
        verify(securityUtils).requireAdmin();
    }

    @Test
    void getUserById_checksAccess() {
        doNothing().when(securityUtils).requireSelfOrAdmin(1L);
        UserEntity user = new UserEntity(1L, "jdoe", "John Doe", "pass", "john@example.com", new ArrayList<>());
        user.setRoles(Set.of(Role.ROLE_USER));
        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));

        UserResponseDTO dto = userService.getUserById(1L);

        assertThat(dto.username()).isEqualTo("jdoe");
        verify(securityUtils).requireSelfOrAdmin(1L);
    }

    @Test
    void createUser_adminEncodesPassword() {
        given(securityUtils.isAdmin()).willReturn(true);
        given(passwordEncoder.encode("Password1")).willReturn("encoded");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserCreateRequestDTO req = new UserCreateRequestDTO("jdoe", "Password1", "John Doe", "john@example.com");
        UserResponseDTO dto = userService.createUser(req);

        assertThat(dto.username()).isEqualTo("jdoe");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().getRoles()).containsExactly(Role.ROLE_USER);
    }

    @Test
    void createUser_nonAdminThrows() {
        given(securityUtils.isAdmin()).willReturn(false);

        UserCreateRequestDTO req = new UserCreateRequestDTO("jdoe", "Password1", "John Doe", "john@example.com");

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateMe_updatesProfileAndEncodesPassword() {
        UserEntity me = new UserEntity(5L, "old", "Old Name", "oldPass", "old@example.com", new ArrayList<>());
        given(securityUtils.requireCurrentUser()).willReturn(me);
        given(passwordEncoder.encode("Password1")).willReturn("encoded");
        given(userRepository.save(me)).willReturn(me);

        UserUpdateRequestDTO req = new UserUpdateRequestDTO("newUser", "Password1", "New Name", "new@example.com");
        UserResponseDTO dto = userService.updateMe(req);

        assertThat(dto.username()).isEqualTo("newUser");
        assertThat(me.getPassword()).isEqualTo("encoded");
        assertThat(me.getFullName()).isEqualTo("New Name");
    }

    @Test
    void adminUpdateUser_updatesAndEncodes() {
        given(securityUtils.isAdmin()).willReturn(true);
        UserEntity user = new UserEntity(3L, "user", "User", "pass", "user@example.com", new ArrayList<>());
        given(userRepository.findById(3L)).willReturn(java.util.Optional.of(user));
        given(passwordEncoder.encode("Password1")).willReturn("encoded");
        given(userRepository.save(user)).willReturn(user);

        AdminUserUpdateRequestDTO req = new AdminUserUpdateRequestDTO("admin", "Password1", "Admin", "admin@example.com", Set.of(Role.ROLE_ADMIN));
        UserResponseDTO dto = userService.adminUpdateUser(3L, req);

        assertThat(dto.username()).isEqualTo("admin");
        assertThat(user.getRoles()).containsExactly(Role.ROLE_ADMIN);
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void deleteUser_requiresAdminAndDeletes() {
        given(securityUtils.isAdmin()).willReturn(true);
        given(userRepository.existsById(4L)).willReturn(true);

        userService.deleteUser(4L);

        verify(userRepository).deleteById(4L);
    }

    @Test
    void deleteUser_missingThrows() {
        given(securityUtils.isAdmin()).willReturn(true);
        given(userRepository.existsById(4L)).willReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(4L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getCurrentUserEntity_usesCustomUserDetails() {
        UserEntity user = new UserEntity(10L, "jdoe", "John Doe", "pass", "john@example.com", new ArrayList<>());
        CustomUserDetails details = new CustomUserDetails(user);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(details, "password");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(userRepository.findById(10L)).willReturn(java.util.Optional.of(user));

        UserEntity result = userService.getCurrentUserEntity();

        assertThat(result).isSameAs(user);
    }
}