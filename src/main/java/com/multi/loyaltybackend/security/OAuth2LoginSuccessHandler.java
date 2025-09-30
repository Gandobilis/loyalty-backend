package com.multi.loyaltybackend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Create a fake authentication object to generate token
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        Authentication fakeAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, null, authentication.getAuthorities());

        String token = tokenProvider.generateToken(fakeAuth);

        // Redirect to frontend with JWT
        String targetUrl = "http://localhost:3000/oauth2/redirect?token=" + token; // Example frontend URL
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}