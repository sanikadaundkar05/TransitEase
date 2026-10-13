package com.transitease.security;

import com.transitease.entity.Role;
import com.transitease.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "TransitEaseSecretKeyForJwtAuthenticationTransitEase2026SecurityKey!";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void testGenerateToken_AndExtractClaims() {
        User user = new User();
        user.setId(10L);
        user.setFullName("Test User");
        user.setEmail("test@transitease.com");
        user.setRole(Role.PASSENGER);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("test@transitease.com", jwtService.extractEmail(token));
        assertEquals("PASSENGER", jwtService.extractRole(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_WithUserDetails() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);

        UserDetails matchingUser = new org.springframework.security.core.userdetails.User(
                "user@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        UserDetails mismatchedUser = new org.springframework.security.core.userdetails.User(
                "other@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER"))
        );

        assertTrue(jwtService.validateToken(token, matchingUser));
        assertFalse(jwtService.validateToken(token, mismatchedUser));
    }

    @Test
    void testValidateToken_TamperedToken_ReturnsFalse() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.PASSENGER);

        String token = jwtService.generateToken(user);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // JwtService with 0ms expiration
        JwtService expiredJwtService = new JwtService(secret, -1000);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.PASSENGER);

        String token = expiredJwtService.generateToken(user);

        assertFalse(jwtService.validateToken(token));
    }
}
