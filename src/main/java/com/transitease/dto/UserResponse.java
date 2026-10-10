package com.transitease.dto;

import com.transitease.entity.Role;
import com.transitease.entity.User;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String message;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, Role role, String message) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public static UserResponse fromUser(User user, String message) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                message
        );
    }

    public static UserResponse fromUser(User user) {
        return fromUser(user, "User registered successfully");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
