package com.example.bankcards.security;

import com.example.bankcards.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


/**
 * Implementation of Spring Security's {@link org.springframework.security.core.userdetails.UserDetails}
 * interface that adapts a {@link UserEntity} to the authentication framework.
 * <p>
 * This class serves as a bridge between the application's domain user model
 * and Spring Security, exposing user credentials, roles, and identifying
 * information required during authentication and authorization.
 * <p>
 * All account status checks (non-expired, non-locked, credentials-valid)
 * return {@code true} by default, assuming that account lifecycle management
 * is handled externally or not yet implemented.
 *
 * @see UserEntity
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see org.springframework.security.core.GrantedAuthority
 * @see org.springframework.security.core.authority.SimpleGrantedAuthority
 */
public class CustomUserDetails implements UserDetails {
    private final UserEntity user;

    public CustomUserDetails(UserEntity userEntity) {
        this.user = userEntity;
    }


    /**
     * Returns the collection of {@link GrantedAuthority} objects assigned to the user.
     * <p>
     * Each {@link Role} in the underlying {@link UserEntity} is converted into a
     * {@link SimpleGrantedAuthority} with the role name (e.g., {@code ROLE_ADMIN}).
     *
     * @return collection of granted authorities for this user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }


    /**
     * Returns the password hash stored for the user.
     *
     * @return encoded password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }


    /**
     * Returns the username used to authenticate the user.
     * <p>
     * This value corresponds to the {@code username} field of {@link UserEntity}.
     *
     * @return username of the user
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }


    /**
     * Returns the email address of the user, if available.
     * <p>
     * This is not part of the {@link UserDetails} contract but is provided
     * for convenience when accessing user information.
     *
     * @return email address of the user
     */
    public String getEmail() {
        return user.getEmail();
    }


    /**
     * Returns the unique identifier of the user.
     * <p>
     * This is a custom property not part of the {@link UserDetails} interface.
     *
     * @return ID of the user entity
     */
    public Long getId() { return user.getId(); }


    /**
     * Indicates whether the user's account has expired.
     * <p>
     * Always returns {@code true} (account is valid) unless account expiration
     * logic is implemented separately.
     *
     * @return {@code true} if the account is non-expired
     */
    @Override
    public boolean isAccountNonExpired() { return true; }


    /**
     * Indicates whether the user is locked or unlocked.
     * <p>
     * Always returns {@code true} (account is unlocked) by default.
     *
     * @return {@code true} if the account is non-locked
     */
    @Override
    public boolean isAccountNonLocked() { return true; }


    /**
     * Indicates whether the user's credentials (password) have expired.
     * <p>
     * Always returns {@code true} (credentials are valid) by default.
     *
     * @return {@code true} if credentials are non-expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    /**
     * Indicates whether the user is enabled or disabled.
     * <p>
     * Always returns {@code true} by default. Application-specific logic
     * may override this if user activation or suspension is implemented later.
     *
     * @return {@code true} if the user is enabled
     */
    @Override
    public boolean isEnabled() { return true; }
}
