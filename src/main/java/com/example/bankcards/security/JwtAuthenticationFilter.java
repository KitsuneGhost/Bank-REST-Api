package com.example.bankcards.security;
import com.example.bankcards.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * Servlet filter that intercepts HTTP requests to perform JWT authentication.
 * <p>
 * Extends {@link org.springframework.web.filter.OncePerRequestFilter} to ensure
 * that token validation and authentication are performed only once per request.
 * <p>
 * The filter extracts a bearer token from the {@code Authorization} header,
 * validates it using {@link JwtUtils}, and if valid, loads user details via
 * {@link CustomUserDetailsService}. It then populates the
 * {@link org.springframework.security.core.context.SecurityContext} with a
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 * representing the authenticated user.
 * <p>
 * If the token is missing or invalid, the request continues unauthenticated,
 * allowing Spring Security to handle access decisions downstream.
 *
 * @see JwtUtils
 * @see CustomUserDetailsService
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }


    /**
     * Processes each incoming HTTP request to extract and validate a JWT token.
     * <p>
     * If a valid bearer token is present and no authentication exists in the
     * {@link org.springframework.security.core.context.SecurityContextHolder},
     * the filter:
     * <ol>
     *   <li>Extracts the username from the token</li>
     *   <li>Loads user details via {@link CustomUserDetailsService}</li>
     *   <li>Creates a {@link UsernamePasswordAuthenticationToken}</li>
     *   <li>Populates the {@code SecurityContext} with the authenticated user</li>
     * </ol>
     * <p>
     * If no token is found or validation fails, the filter simply passes the
     * request down the filter chain without modifying the security context.
     *
     * @param request  incoming {@link HttpServletRequest}
     * @param response outgoing {@link HttpServletResponse}
     * @param chain    the {@link FilterChain} for continuing request processing
     * @throws ServletException if an internal error occurs during filtering
     * @throws IOException      if an I/O error occurs while processing the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (jwtUtils.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtUtils.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}