package com.transitease.controller;

import com.transitease.dto.LoginRequest;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("encodedPassword123");
        user.setRole(Role.PASSENGER);

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(user);

        String requestJson = """
                {
                    "email": "jane@example.com",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testLogin_UnknownEmail() throws Exception {
        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Email not found"));

        String requestJson = """
                {
                    "email": "unknown@example.com",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email not found"));
    }

    @Test
    void testLogin_IncorrectPassword() throws Exception {
        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Incorrect password"));

        String requestJson = """
                {
                    "email": "jane@example.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Incorrect password"));
    }

    @Test
    void testLogin_ValidationFailure_BlankFields() throws Exception {
        String requestJson = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void testLogin_ValidationFailure_InvalidEmail() throws Exception {
        String requestJson = """
                {
                    "email": "not-an-email",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").value("Enter a valid email"));
    }

    @Test
    void testLogin_PasswordNeverInResponse() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setFullName("John Smith");
        user.setEmail("john@example.com");
        user.setPassword("superSecretHashedPassword");
        user.setRole(Role.ADMIN);

        when(userService.loginUser(any(LoginRequest.class))).thenReturn(user);

        String requestJson = """
                {
                    "email": "john@example.com",
                    "password": "correctPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
