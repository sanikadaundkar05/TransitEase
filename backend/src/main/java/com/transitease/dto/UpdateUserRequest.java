package com.transitease.dto;

import com.transitease.entity.Role;

public class UpdateUserRequest {

    private String fullName;
    private Role role;
    private String password;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String fullName, Role role, String password) {
        this.fullName = fullName;
        this.role = role;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
