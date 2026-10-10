package com.transitease.controller;

import com.transitease.dto.LoginRequest;
import com.transitease.dto.LoginResponse;
import com.transitease.entity.User;
import com.transitease.security.JwtService;
import com.transitease.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserLoginController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserLoginController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.loginUser(request);
        String token = jwtService.generateToken(user);
        LoginResponse response = LoginResponse.fromUser(user, token, "Login successful");
        return ResponseEntity.ok(response);
    }
}
