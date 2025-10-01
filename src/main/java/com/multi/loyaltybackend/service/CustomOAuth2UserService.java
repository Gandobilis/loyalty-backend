package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Fetch user details from the OAuth2 provider
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // 2. Find or create/update a user in the local database
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    // Update existing user's name in case it changed
                    existingUser.setUsername(name);
                    return existingUser;
                })
                .orElseGet(() -> {
                    // Create a new user if not found
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setRole(Role.ROLE_USER);
                    // Password can be null for OAuth2 users
                    newUser.setPassword(null);
                    return userRepository.save(newUser);
                });

        // 3. Link the OAuth2 attributes to our User object and return it
        user.setAttributes(oauth2User.getAttributes());
        return user;
    }
}