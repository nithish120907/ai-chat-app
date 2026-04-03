package com.chatapp.controller;

import com.chatapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping("/test")
public String test() {
    return "Auth working";
}

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {

        String username = request.get("username");

        String token = jwtUtil.generateToken(username);

        return Map.of("token", token);
    }
}