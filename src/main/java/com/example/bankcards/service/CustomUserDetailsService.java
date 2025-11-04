package com.example.bankcards.service;

import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bankcards.entity.UserEntity;


/**
 * Service component that integrates the application's {@link UserEntity}
 * persistence with Spring Security's authentication mechanism.
 * <p>
 * Implements {@link org.springframework.security.core.userdetails.UserDetailsService}
 * to load user-specific data during the authentication process.
 * <p>
 * This service retrieves a user from the {@link UserRepository} by username
 * and wraps it into a {@link CustomUserDetails} object, which provides
 * Spring Security with roles, credentials, and account status information.
 *
 * @see CustomUserDetails
 * @see UserRepository
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates a user by their username and returns a {@link CustomUserDetails} instance
     * for authentication by Spring Security.
     * <p>
     * This method is invoked automatically by Spring Security's
     * {@code DaoAuthenticationProvider} during login attempts.
     * <p>
     * If the specified username is not found in the database, a
     * {@link UsernameNotFoundException} is thrown, which prevents authentication.
     *
     * @param username username or unique identifier of the user
     * @return {@link CustomUserDetails} containing user credentials and authorities
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
            return new CustomUserDetails(user);
    }
}
