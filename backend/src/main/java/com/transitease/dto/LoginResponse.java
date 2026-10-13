package com.transitease.dto;

import com.transitease.entity.Role;
import com.transitease.entity.User;

public class LoginResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String token;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(Long id, String fullName, String email, Role role, String token, String message) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.token = token;
        this.message = message;
    }

    public LoginResponse(Long id, String fullName, String email, Role role, String message) {
        this(id, fullName, email, role, null, message);
    }

    public static LoginResponse fromUser(User user, String token, String message) {
        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                token,
                message
        );
    }

    public static LoginResponse fromUser(User user, String token) {
        return fromUser(user, token, "Login successful");
    }

    public static LoginResponse fromUser(User user) {
        return fromUser(user, null, "Login successful");
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
