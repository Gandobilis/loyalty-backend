package com.multi.loyaltybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration.class
})
@EnableJpaAuditing
public class LoyaltyBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyBackendApplication.class, args);
    }
}