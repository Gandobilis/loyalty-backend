package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.RegisterRequest;
import com.multi.loyaltybackend.exception.UserAlreadyExistsException;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest DTO containing username, email, and password.
     */
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new UserAlreadyExistsException("Username " + registerRequest.username() + " is already taken.");
        }
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new UserAlreadyExistsException("Email " + registerRequest.email() + " is already registered.");
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setRole(Role.ROLE_USER); // Default role for new users

        userRepository.save(user);
    }

    /**
     * Creates a password reset token and sends it to the user's email.
     *
     * @param email The email of the user requesting a password reset.
     */
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User with email " + email + " not found."));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token is valid for 1 hour
        userRepository.save(user);

        sendPasswordResetEmail(email, token);
    }

    /**
     * Resets the user's password if the provided token is valid.
     *
     * @param token       The password reset token.
     * @param newPassword The new password to set.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid password reset token."));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Password reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null); // Invalidate the token after use
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Helper method to send the password reset email.
     *
     * @param to    The recipient's email address.
     * @param token The password reset token.
     */
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        // The URL should point to your frontend application's reset password page
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetUrl);

        mailSender.send(message);
    }
}