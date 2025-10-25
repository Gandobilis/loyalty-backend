package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful. Please check your email for verification code.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
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

    // New code-based password reset endpoints

    @PostMapping("/forget-password-code")
    public ResponseEntity<String> forgetPasswordWithCode(@Valid @RequestBody PasswordResetRequest request) {
        authService.initiatePasswordResetWithCode(request.email());
        return ResponseEntity.ok("Password reset code sent to your email.");
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, Object>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        boolean isValid = authService.verifyResetCode(request.email(), request.code());
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", "Reset code is valid");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password-code")
    public ResponseEntity<String> resetPasswordWithCode(@Valid @RequestBody PasswordResetCodeRequest request) {
        authService.resetPasswordWithCode(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    // Email verification endpoints

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.email(), request.code());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully. You can now login.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationCode(request.email());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification code sent to your email.");
        return ResponseEntity.ok(response);
    }
}