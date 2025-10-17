package com.multi.loyaltybackend.auth.service;

import com.multi.loyaltybackend.auth.model.Role;
import com.multi.loyaltybackend.auth.model.User;
import com.multi.loyaltybackend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault("name", "Unknown");
        System.out.println(attributes);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user exists or create new
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() -> createNewUser(email, name));

        // Update info if needed (for example: fullName changed in Google)
        if (!name.equals(user.getFullName())) {
            user.setFullName(name);
            userRepository.save(user);
        }

        // Return a user compatible with Spring Security
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                "email" // The key used to get the username
        );
    }

    private User createNewUser(String email, String name) {
        User newUser = User.builder()
                .email(email)
                .fullName(name)
                .password(null) // No password for OAuth users
                .role(Role.USER)
                .build();

        return userRepository.save(newUser);
    }
}
