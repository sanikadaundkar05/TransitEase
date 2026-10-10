package com.transitease.service;

import com.transitease.dto.LoginRequest;
import com.transitease.dto.RegisterRequest;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("encodedPassword123", result.getPassword());
        assertEquals(Role.PASSENGER, result.getRole());

        verify(userRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword123");
        user.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);

        User result = userService.loginUser(loginRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(Role.PASSENGER, result.getRole());

        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword123");
    }

    @Test
    void testLoginUser_UnknownEmail_ThrowsException() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.loginUser(loginRequest));

        assertEquals("Email not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testLoginUser_IncorrectPassword_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword123");
        user.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.loginUser(loginRequest));

        assertEquals("Incorrect password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword123");
    }
}
