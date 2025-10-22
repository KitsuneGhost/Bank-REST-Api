package com.example.bankcards.controller;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.security.Role;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.bankcards\\.security\\..*")
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.MockConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void resetMocks() {
        reset(userService);
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        List<UserResponseDTO> users = List.of(
                new UserResponseDTO(1L, "alice", "Alice Doe", "alice@example.com",
                        Set.of("ROLE_USER"))
        );
        given(userService.getAllUsers()).willReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void getById_returnsUser() throws Exception {
        UserResponseDTO user =
                new UserResponseDTO(5L, "bob", "Bob Marley", "bob@example.com",
                        Set.of("ROLE_USER"));
        given(userService.getUserById(5L)).willReturn(user);

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Bob Marley"));
    }

    @Test
    void create_createsUser() throws Exception {
        UserCreateRequestDTO req = new UserCreateRequestDTO("john", "Password1", "John Doe",
                "john@example.com");
        UserResponseDTO resp = new UserResponseDTO(10L, "john", "John Doe",
                "john@example.com", Set.of("ROLE_USER"));
        given(userService.createUser(any(UserCreateRequestDTO.class))).willReturn(resp);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void adminUpdate_updatesUser() throws Exception {
        AdminUserUpdateRequestDTO req =
                new AdminUserUpdateRequestDTO("admin", "AdmPass1", "Admin User",
                        "admin@example.com", Set.of(Role.ROLE_ADMIN));
        UserResponseDTO resp =
                new UserResponseDTO(7L, "admin", "Admin User", "admin@example.com",
                        Set.of("ROLE_ADMIN"));
        given(userService.adminUpdateUser(eq(7L), any(AdminUserUpdateRequestDTO.class))).willReturn(resp);

        mockMvc.perform(put("/users/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void delete_removesUser() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(4L);
    }

    @Test
    void getMe_returnsCurrentProfile() throws Exception {
        UserResponseDTO me = new UserResponseDTO(99L, "self", "Current User",
                "me@example.com", Set.of("ROLE_USER"));
        given(userService.getMe()).willReturn(me);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("self"));
    }

    @Test
    void updateMe_updatesProfile() throws Exception {
        UserUpdateRequestDTO req =
                new UserUpdateRequestDTO("self", "Password1", "Updated Me",
                        "me@example.com");
        UserResponseDTO resp =
                new UserResponseDTO(99L, "self", "Updated Me", "me@example.com",
                        Set.of("ROLE_USER"));
        given(userService.updateMe(any(UserUpdateRequestDTO.class))).willReturn(resp);

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Me"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean @Primary UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean @Primary CardService cardService() {
            return Mockito.mock(CardService.class);
        }
    }
}
