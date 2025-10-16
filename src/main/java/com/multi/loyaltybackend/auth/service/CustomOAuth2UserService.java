package com.multi.loyaltybackend.auth.service;

import com.multi.loyaltybackend.auth.model.CustomOAuth2User;
import com.multi.loyaltybackend.auth.model.Role;
import com.multi.loyaltybackend.auth.model.User;
import com.multi.loyaltybackend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oauth2User, provider);
        String name = extractName(oauth2User, provider);
        String providerId = oauth2User.getName(); // OAuth provider's user ID

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, provider, providerId))
                .orElseGet(() -> createNewUser(email, name, provider, providerId));

        return new CustomOAuth2User(oauth2User, user);
    }

    private String extractEmail(OAuth2User oauth2User, String provider) {
        return oauth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oauth2User, String provider) {
        if ("github".equals(provider)) {
            return oauth2User.getAttribute("name") != null
                    ? oauth2User.getAttribute("name")
                    : oauth2User.getAttribute("login");
        }
        return oauth2User.getAttribute("name");
    }

    private User updateExistingUser(User user, String provider, String providerId) {
        if (user.getAuthProvider() == null || user.getAuthProvider().equals("local")) {
            user.setAuthProvider(provider);
            user.setProviderId(providerId);
            return userRepository.save(user);
        }
        return user;
    }

    private User createNewUser(String email, String name, String provider, String providerId) {
        User user = User.builder()
                .email(email)
                .fullName(name != null ? name : email.split("@")[0])
                .password("") // OAuth users don't need passwords
                .authProvider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }
}