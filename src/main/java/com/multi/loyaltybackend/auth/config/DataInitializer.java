package com.multi.loyaltybackend.auth.config;

import com.multi.loyaltybackend.auth.model.ERole;
import com.multi.loyaltybackend.auth.model.Role;
import com.multi.loyaltybackend.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // Check if ROLE_USER exists, if not, create it
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER));
            }

            // Check if ROLE_ADMIN exists, if not, create it
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }
        };
    }
}
