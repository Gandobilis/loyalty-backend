package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.security.JwtTokenProvider;
import com.multi.loyaltybackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for user registration, login, and password management")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    @Operation(summary = "Authenticate a user", description = "Logs in a user with their email and password, returning a JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponze<AuthResponse>> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(ApiResponze.success(new AuthResponse(jwt), "Login successful."));
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponze<Void>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>(ApiResponze.success("User registered successfully!"), HttpStatus.CREATED);
    }

    @Operation(summary = "Initiate password reset", description = "Sends a password reset link to the user's email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset link sent"),
            @ApiResponse(responseCode = "400", description = "Invalid email format"),
            @ApiResponse(responseCode = "404", description = "User with the specified email not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponze<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        authService.createPasswordResetToken(resetRequest.email());
        return ResponseEntity.ok(ApiResponze.success("Password has been reset successfully."));
    }

    @Operation(summary = "Reset user password", description = "Sets a new password using a valid reset token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password has been reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or new password format"),
            @ApiResponse(responseCode = "404", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponze<Void>> resetPassword(@Valid @RequestBody NewPasswordRequest newPasswordRequest) {
        authService.resetPassword(newPasswordRequest.token(), newPasswordRequest.newPassword());
        return ResponseEntity.ok(ApiResponze.success("Password has been reset successfully."));
    }
}