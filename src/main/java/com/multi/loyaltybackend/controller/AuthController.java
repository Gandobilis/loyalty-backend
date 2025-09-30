package com.multi.loyaltybackend.controller;


import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.security.JwtTokenProvider;
import com.multi.loyaltybackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest resetRequest) {
        authService.createPasswordResetToken(resetRequest.email());
        return ResponseEntity.ok("Password reset link sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest newPasswordRequest) {
        authService.resetPassword(newPasswordRequest.token(), newPasswordRequest.newPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}
