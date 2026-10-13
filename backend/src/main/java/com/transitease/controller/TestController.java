package com.transitease.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "TransitEase backend is working!";
    }

    @GetMapping("/api/test/protected")
    public String protectedTest() {
        return "Protected test endpoint accessed successfully!";
    }

    @GetMapping("/api/admin/test")
    public String adminTest() {
        return "Admin access successful!";
    }

    @GetMapping("/api/passenger/test")
    public String passengerTest() {
        return "Passenger access successful!";
    }
}