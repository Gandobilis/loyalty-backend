package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/success")
    public void oauth2Success(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletResponse response
    ) throws IOException {
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            response.sendRedirect(frontendUrl + "/login?error=email_not_found");
            return;
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 authentication"));

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Redirect to frontend with token
        response.sendRedirect(frontendUrl + "/oauth2/redirect?token=" + token);
    }

    @GetMapping("/failure")
    public void oauth2Failure(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/login?error=oauth2_failed");
    }
}
