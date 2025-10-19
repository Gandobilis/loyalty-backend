package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.dto.AuthRequest;
import com.multi.loyaltybackend.dto.PasswordResetRequest;
import com.multi.loyaltybackend.dto.PasswordUpdateRequest;
import com.multi.loyaltybackend.dto.RegisterRequest;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = User.builder()
                .email(request.email())
                .password(request.password())
                .role(request.role())
                .fullName(request.fullName())
                .build();
        authService.register(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.email(), request.password());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
            return ResponseEntity.ok("Logged out successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid request.");
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@RequestBody PasswordResetRequest request) {
        authService.initiatePasswordReset(request.email());
        return ResponseEntity.ok("Password reset link sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordUpdateRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}