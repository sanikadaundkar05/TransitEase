package com.transitease.security;

import com.transitease.dto.LoginRequest;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @Test
    void testProtectedEndpoint_WithMissingJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void testProtectedEndpoint_WithInvalidJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer invalid.jwt.signature"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void testProtectedEndpoint_WithValidJwt_ReturnsOk() throws Exception {
        User user = new User();
        user.setId(5L);
        user.setFullName("Auth User");
        user.setEmail("authuser@transitease.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("authuser@transitease.com")).thenReturn(Optional.of(user));

        String validToken = jwtService.generateToken(user);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Protected test endpoint accessed successfully!"));
    }

    @Test
    void testPublicEndpoints_AccessibleWithoutJwt() throws Exception {
        // /api/test is public
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("TransitEase backend is working!"));

        // /api/auth/register is public (bad request on empty body, not 401)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // /api/auth/login is public (bad request on empty body, not 401)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_ReturnsJwt_AndPasswordNeverReturned() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.PASSENGER);

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(user);

        String loginJson = """
                {
                    "email": "jane@example.com",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
