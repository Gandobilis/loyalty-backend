package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.exception.*;
import com.multi.loyaltybackend.model.PasswordResetCode;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.PasswordResetCodeRepository;
import com.multi.loyaltybackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    // Thread-safe set for blacklisted tokens
    // TODO: Consider using Redis with TTL for production to prevent memory leaks
    public static final Set<String> blackList = ConcurrentHashMap.newKeySet();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService, PasswordResetCodeRepository passwordResetCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
    }

    public void register(User request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }


        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null ? Role.USER : request.getRole())
                .fullName(request.getFullName())
                .build();

        userRepository.save(user);
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtService.generateToken(user);
    }

    public void logout(String token) {
        blackList.add(token);
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        // Set token expiration to 1 hour from now
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException());

        // Check if token has expired
        if (user.getPasswordResetTokenExpiry() == null ||
            user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    // New code-based password reset methods

    /**
     * Generates a 6-digit secure random code
     */
    private String generateResetCode() {
        int code = secureRandom.nextInt(900000) + 100000; // Generates number between 100000 and 999999
        return String.valueOf(code);
    }

    /**
     * Initiates password reset with a 6-digit code sent via email
     */
    @Transactional
    public void initiatePasswordResetWithCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Delete any existing codes for this email
        passwordResetCodeRepository.deleteByEmail(email);

        // Generate new reset code
        String code = generateResetCode();
        PasswordResetCode resetCode = PasswordResetCode.builder()
                .code(code)
                .email(email)
                .expiryTime(LocalDateTime.now().plusMinutes(15)) // Code expires in 15 minutes
                .used(false)
                .build();

        passwordResetCodeRepository.save(resetCode);
        emailService.sendPasswordResetCode(email, code);
    }

    /**
     * Verifies if the provided reset code is valid
     */
    public boolean verifyResetCode(String email, String code) {
        PasswordResetCode resetCode = passwordResetCodeRepository.findByCodeAndEmail(code, email)
                .orElseThrow(() -> new InvalidPasswordResetCodeException("Invalid reset code"));

        if (resetCode.isExpired()) {
            throw new PasswordResetCodeExpiredException();
        }

        if (resetCode.isUsed()) {
            throw new InvalidPasswordResetCodeException("Reset code has already been used");
        }

        return true;
    }

    /**
     * Resets password using the code-based flow
     */
    @Transactional
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        PasswordResetCode resetCode = passwordResetCodeRepository.findByCodeAndEmail(code, email)
                .orElseThrow(() -> new InvalidPasswordResetCodeException("Invalid reset code"));

        if (resetCode.isExpired()) {
            throw new PasswordResetCodeExpiredException();
        }

        if (resetCode.isUsed()) {
            throw new InvalidPasswordResetCodeException("Reset code has already been used");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark code as used
        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);
    }

    /**
     * Cleanup method to delete expired reset codes
     * Should be called periodically (e.g., via scheduled task)
     */
    @Transactional
    public void cleanupExpiredResetCodes() {
        passwordResetCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}