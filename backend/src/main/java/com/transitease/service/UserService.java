package com.transitease.service;

import com.transitease.dto.LoginRequest;
import com.transitease.dto.RegisterRequest;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PASSENGER);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public java.util.List<com.transitease.dto.UserResponse> getAllUsers(Role role) {
        java.util.List<User> users;
        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream()
                .map(u -> com.transitease.dto.UserResponse.fromUser(u, "User details retrieved successfully"))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.transitease.dto.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.transitease.exception.ResourceNotFoundException("User not found with id: " + id));
        return com.transitease.dto.UserResponse.fromUser(user, "User details retrieved successfully");
    }

    @Transactional
    public com.transitease.dto.UserResponse createStaffUser(com.transitease.dto.CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);
        return com.transitease.dto.UserResponse.fromUser(saved, "Staff account created successfully");
    }

    @Transactional
    public com.transitease.dto.UserResponse updateUser(Long id, com.transitease.dto.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.transitease.exception.ResourceNotFoundException("User not found with id: " + id));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        User updated = userRepository.save(user);
        return com.transitease.dto.UserResponse.fromUser(updated, "User updated successfully");
    }

    @Transactional
    public com.transitease.dto.UserResponse updateUserRole(Long id, Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.transitease.exception.ResourceNotFoundException("User not found with id: " + id));
        user.setRole(newRole);
        User updated = userRepository.save(user);
        return com.transitease.dto.UserResponse.fromUser(updated, "User role updated successfully");
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.transitease.exception.ResourceNotFoundException("User not found with id: " + id));

        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String currentEmail = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            if (user.getEmail().equalsIgnoreCase(currentEmail)) {
                throw new IllegalStateException("Cannot delete the currently logged in administrator account");
            }
        }

        userRepository.delete(user);
    }
}