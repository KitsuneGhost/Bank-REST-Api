package com.example.bankcards.controller;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService, CardService cardService) {
        this.userService = userService;
    }

    /** ADMIN: list all users */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /** ADMIN or self: get one by id */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /** SELF: convenient 'me' endpoint */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponseDTO getMe() {
        return userService.getMe();
    }

    /** ADMIN: create user */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserCreateRequestDTO req) {
        return userService.createUser(req);
    }

    /** SELF: update my profile */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponseDTO updateMe(@Valid @RequestBody UserUpdateRequestDTO req) {
        return userService.updateMe(req);
    }

    /** ADMIN: update any user */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO adminUpdate(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequestDTO req) {
        return userService.adminUpdateUser(id, req);
    }

    /** ADMIN: delete user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
