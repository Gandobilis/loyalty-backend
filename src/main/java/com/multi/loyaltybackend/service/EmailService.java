package com.multi.loyaltybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.frontend.url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetUrl);

        mailSender.send(message);
    }

    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code + "\n\n" +
                "This code will expire in 15 minutes.\n\n" +
                "If you did not request a password reset, please ignore this email.");

        mailSender.send(message);
    }

    public void sendEmailVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification Code");
        message.setText("Welcome! Your email verification code is: " + code + "\n\n" +
                "This code will expire in 15 minutes.\n\n" +
                "Please verify your email to complete your registration.");

        mailSender.send(message);
    }
}