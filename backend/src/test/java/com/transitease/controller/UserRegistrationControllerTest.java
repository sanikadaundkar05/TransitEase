package com.transitease.controller;

import com.transitease.dto.RegisterRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void testRegisterUser_Success() throws Exception {
        User savedUser = new User();
        savedUser.setFullName("Jane Doe");
        savedUser.setEmail("jane@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.PASSENGER);

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(savedUser);

        String requestJson = """
                {
                    "fullName": "Jane Doe",
                    "email": "jane@example.com",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testRegisterUser_ValidationFailure_BlankFields() throws Exception {
        String requestJson = """
                {
                    "fullName": "",
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void testRegisterUser_ValidationFailure_InvalidEmail() throws Exception {
        String requestJson = """
                {
                    "fullName": "Jane Doe",
                    "email": "not-an-email",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").value("Enter a valid email"));
    }

    @Test
    void testRegisterUser_ValidationFailure_ShortPassword() throws Exception {
        String requestJson = """
                {
                    "fullName": "Jane Doe",
                    "email": "jane@example.com",
                    "password": "12345"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 6 characters"));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already registered"));

        String requestJson = """
                {
                    "fullName": "Jane Doe",
                    "email": "existing@example.com",
                    "password": "secret123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void testTestEndpoint_RemainsWorking() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk());
    }
}
