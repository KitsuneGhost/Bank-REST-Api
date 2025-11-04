package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.dto.auth.LoginResponse;
import com.example.bankcards.dto.auth.RegisterRequest;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.Role;
import com.example.bankcards.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * REST controller responsible for handling authentication-related operations.
 * <p>
 * This controller exposes public endpoints for user login and registration.
 * It issues JSON Web Tokens (JWT) upon successful authentication and stores
 * newly registered users with securely encoded passwords.
 *
 * <p><b>Endpoints:</b>
 * <ul>
 *   <li>{@code POST /auth/login} — Authenticates a user and returns a JWT token.</li>
 *   <li>{@code POST /auth/register} — Creates a new user account.</li>
 * </ul>
 *
 * <p>All endpoints are publicly accessible and do not require authentication.
 * Role-based access control applies to other parts of the API after login.
 *
 * @see com.example.bankcards.security.JwtUtils
 * @see com.example.bankcards.security.CustomUserDetails
 * @see com.example.bankcards.dto.auth.LoginRequest
 * @see com.example.bankcards.dto.auth.LoginResponse
 * @see com.example.bankcards.dto.auth.RegisterRequest
 * @see com.example.bankcards.entity.UserEntity
 * @see org.springframework.security.authentication.AuthenticationManager
 */
@Tag(name = "Auth", description = "Authentication endpoints")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtUtils jwtUtils,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Authenticates a user using username and password credentials, and returns a JWT token.
     * <p>
     * This endpoint verifies user credentials via {@link AuthenticationManager}, and upon success,
     * generates a signed JWT using {@link JwtUtils}. The token can then be used to authorize
     * requests to protected endpoints.
     *
     * <p><b>Request:</b>
     * <pre>
     * POST /auth/login
     * {
     *   "username": "johndoe",
     *   "password": "securePassword"
     * }
     * </pre>
     *
     * <p><b>Response:</b>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "expiresInMs": 86400000
     * }
     * </pre>
     *
     * @param req the login request containing username and password
     * @return {@link com.example.bankcards.dto.auth.LoginResponse} containing the JWT token and expiration
     * @throws org.springframework.security.authentication.BadCredentialsException if authentication fails
     *
     * @see com.example.bankcards.dto.auth.LoginRequest
     * @see com.example.bankcards.dto.auth.LoginResponse
     * @see com.example.bankcards.security.JwtUtils
     */
    @Operation(summary = "Login (returns JWT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtils.generateToken(cud);
        return new LoginResponse(token, 24*60*60*1000);
    }


    /**
     * Registers a new user in the system.
     * <p>
     * Accepts a {@link com.example.bankcards.dto.auth.RegisterRequest} containing basic
     * user data and stores it as a new {@link com.example.bankcards.entity.UserEntity}
     * with encoded password and default role {@code ROLE_USER}.
     *
     * <p><b>Request:</b>
     * <pre>
     * POST /auth/register
     * {
     *   "username": "janedoe",
     *   "email": "jane.doe@example.com",
     *   "fullName": "Jane Doe",
     *   "password": "strongPass123"
     * }
     * </pre>
     *
     * <p><b>Response:</b>
     * <pre>
     * {
     *   "message": "User created"
     * }
     * </pre>
     *
     * @param req the registration request containing new user information
     * @return a confirmation message indicating successful creation
     * @throws org.springframework.dao.DataIntegrityViolationException if username or email already exists
     *
     * @see com.example.bankcards.dto.auth.RegisterRequest
     * @see com.example.bankcards.entity.UserEntity
     */
    @Operation(summary = "Register user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        var user = new UserEntity();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setFullName(req.fullName());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);
        return Map.of("message","User created");
    }
}
