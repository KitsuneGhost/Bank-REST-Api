package com.example.bankcards.controller;

import com.example.bankcards.dto.user.AdminUserUpdateRequestDTO;
import com.example.bankcards.dto.user.UserCreateRequestDTO;
import com.example.bankcards.dto.user.UserResponseDTO;
import com.example.bankcards.dto.user.UserUpdateRequestDTO;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller providing endpoints for user management and profile operations.
 * <p>
 * Includes both administrative operations (accessible to {@code ROLE_ADMIN})
 * and self-service endpoints for authenticated users ({@code ROLE_USER} or {@code ROLE_ADMIN}).
 * <p>
 * Uses {@link com.example.bankcards.service.UserService} for all business logic
 * related to user creation, retrieval, update, and deletion.
 *
 * <p><b>Endpoints Overview:</b>
 * <ul>
 *   <li>{@code GET /users} — List all users (Admin only)</li>
 *   <li>{@code GET /users/{id}} — Retrieve user by ID (Self or Admin)</li>
 *   <li>{@code POST /users} — Create new user (Admin only)</li>
 *   <li>{@code PUT /users/{id}} — Update existing user (Admin only)</li>
 *   <li>{@code DELETE /users/{id}} — Delete a user (Admin only)</li>
 *   <li>{@code GET /users/me} — Retrieve the authenticated user's profile</li>
 *   <li>{@code PUT /users/me} — Update the authenticated user's profile</li>
 * </ul>
 *
 * <p>All endpoints require JWT-based authentication unless otherwise specified.
 *
 * @see com.example.bankcards.service.UserService
 * @see com.example.bankcards.dto.user.UserResponseDTO
 * @see com.example.bankcards.dto.user.UserCreateRequestDTO
 * @see com.example.bankcards.dto.user.UserUpdateRequestDTO
 * @see com.example.bankcards.dto.user.AdminUserUpdateRequestDTO
 * @see io.swagger.v3.oas.annotations.security.SecurityRequirement
 */
@Tag(name = "Users", description = "User self-service and admin operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService, CardService cardService) {
        this.userService = userService;
    }


    /* ========================= FILTERS ========================= */


    /**
     * Retrieves a list of all users.
     * <p>
     * Accessible only to administrators.
     *
     * @return list of {@link UserResponseDTO} representing all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list users", description = "Returns all users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }


    /* ========================= BASIC CRUD ========================= */


    /**
     * Retrieves a user by their ID.
     * <p>
     * Accessible by the user themselves or an administrator.
     *
     * @param id the ID of the user to retrieve
     * @return {@link UserResponseDTO} containing user details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get user by id", description = "Self or ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


    /**
     * Creates a new user account.
     * <p>
     * Accessible only to administrators.
     *
     * @param req request payload containing user information
     * @return {@link UserResponseDTO} representing the newly created user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin: create user", description = "Creates a user with requested attributes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = com.example.bankcards.dto.user.UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public UserResponseDTO create(@Valid @RequestBody UserCreateRequestDTO req) {
        return userService.createUser(req);
    }


    /**
     * Updates an existing user account.
     * <p>
     * Accessible only to administrators. Allows changing user details and roles.
     *
     * @param id  ID of the target user
     * @param req request payload containing updated user attributes
     * @return {@link UserResponseDTO} representing the updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: update user", description = "Updates the target user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public UserResponseDTO adminUpdate(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequestDTO req) {
        return userService.adminUpdateUser(id, req);
    }


    /**
     * Deletes a user by ID.
     * <p>
     * Accessible only to administrators.
     *
     * @param id ID of the user to delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin: delete user", description = "Deletes the target user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }


    /* ========================= CURRENT USER ENDPOINTS ========================= */


    /**
     * Retrieves the authenticated user's own profile.
     * <p>
     * Accessible by any authenticated user.
     *
     * @return {@link UserResponseDTO} representing the current user
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public UserResponseDTO getMe() {
        return userService.getMe();
    }


    /**
     * Updates the authenticated user's profile.
     * <p>
     * Accessible by any authenticated user. Supports partial updates.
     *
     * @param req request body containing new values for the user's own attributes
     * @return {@link UserResponseDTO} representing the updated user
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update my profile", description = "Self-service update for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public UserResponseDTO updateMe(@Valid @RequestBody UserUpdateRequestDTO req) {
        return userService.updateMe(req);
    }

}
