package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.security.JwtTokenProvider;
import com.multi.loyaltybackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );
        String jwt = tokenProvider.generateToken(authentication);
        AuthResponse authResponse = new AuthResponse(jwt);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, authResponse, "Login successful."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>(ApiResponse.success(HttpStatus.CREATED, "User registered successfully!"), HttpStatus.CREATED);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        authService.createPasswordResetToken(resetRequest.email());

        String successMessage = "If an account with the provided email exists, a password reset link has been sent.";
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        authService.resetPassword(newPasswordRequest.token(), newPasswordRequest.newPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Password has been reset successfully."));
    }
}
