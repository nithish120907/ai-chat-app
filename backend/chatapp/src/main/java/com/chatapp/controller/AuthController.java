package com.chatapp.controller;

import com.chatapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        String result = authService.register(
            request.get("username"),
            request.get("password")
        );
        return Map.of("message", result);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String token = authService.login(
            request.get("username"),
            request.get("password")
        );
        return Map.of("token", token);
    }
}