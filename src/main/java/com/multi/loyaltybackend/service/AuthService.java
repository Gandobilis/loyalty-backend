package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.AuthResponse;
import com.multi.loyaltybackend.dto.RegisterRequest;
import com.multi.loyaltybackend.exception.*;
import com.multi.loyaltybackend.model.EmailVerificationCode;
import com.multi.loyaltybackend.model.PasswordResetCode;
import com.multi.loyaltybackend.model.RefreshToken;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.EmailVerificationCodeRepository;
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
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final RefreshTokenService refreshTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    // Thread-safe set for blacklisted tokens
    // TODO: Consider using Redis with TTL for production to prevent memory leaks
    public static final Set<String> blackList = ConcurrentHashMap.newKeySet();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService, PasswordResetCodeRepository passwordResetCodeRepository, EmailVerificationCodeRepository emailVerificationCodeRepository, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role() == null ? Role.USER : request.role())
                .fullName(request.fullName())
                .fileName("default-profile.png")
                .totalPoints(1000)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Generate and send email verification code
        String code = generateVerificationCode();
        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .code(code)
                .email(request.email())
                .expiryTime(LocalDateTime.now().plusMinutes(15)) // Code expires in 15 minutes
                .used(false)
                .build();

        emailVerificationCodeRepository.save(verificationCode);
        emailService.sendEmailVerificationCode(request.email(), code);
    }

    @Transactional
    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = userRepository.findByEmail(email).orElseThrow();

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        // Generate access token
        String accessToken = jwtService.generateToken(user);

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(String token) {
        // Blacklist the access token
        blackList.add(token);

        // Extract user from token and revoke their refresh token
        String userEmail = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            refreshTokenService.revokeUserTokens(user);
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        // Find and validate the refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshTokenService.validateToken(refreshToken);

        // Get the user associated with this refresh token
        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        // Generate new refresh token (token rotation for better security)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
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

    // Email verification methods

    /**
     * Generates a 6-digit secure random verification code
     */
    private String generateVerificationCode() {
        int code = secureRandom.nextInt(900000) + 100000; // Generates number between 100000 and 999999
        return String.valueOf(code);
    }

    /**
     * Verifies email using the provided verification code
     */
    @Transactional
    public void verifyEmail(String email, String code) {
        EmailVerificationCode verificationCode = emailVerificationCodeRepository.findByCodeAndEmail(code, email)
                .orElseThrow(() -> new InvalidEmailVerificationCodeException("Invalid verification code"));

        if (verificationCode.isExpired()) {
            throw new EmailVerificationCodeExpiredException();
        }

        if (verificationCode.isUsed()) {
            throw new InvalidEmailVerificationCodeException("Verification code has already been used");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Mark email as verified
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark code as used
        verificationCode.setUsed(true);
        emailVerificationCodeRepository.save(verificationCode);
    }

    /**
     * Resends email verification code
     */
    @Transactional
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Delete any existing codes for this email
        emailVerificationCodeRepository.deleteByEmail(email);

        // Generate and send new verification code
        String code = generateVerificationCode();
        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .code(code)
                .email(email)
                .expiryTime(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        emailVerificationCodeRepository.save(verificationCode);
        emailService.sendEmailVerificationCode(email, code);
    }

    /**
     * Cleanup method to delete expired verification codes
     * Should be called periodically (e.g., via scheduled task)
     */
    @Transactional
    public void cleanupExpiredVerificationCodes() {
        emailVerificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}