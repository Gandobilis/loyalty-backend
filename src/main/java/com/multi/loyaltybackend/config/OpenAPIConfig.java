package com.multi.loyaltybackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenAPIConfig.class);

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.server.url:#{null}}")
    private String configuredServerUrl;

    @Bean
    public String appServerUrl() {
        // If app.server.url is explicitly configured, use it
        if (configuredServerUrl != null && !configuredServerUrl.isEmpty()) {
            log.info("Using configured server URL: {}", configuredServerUrl);
            return configuredServerUrl;
        }

        // Otherwise, detect dynamically
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String dynamicUrl = "http://" + ip + ":" + serverPort;
            log.info("Dynamically detected server URL: {}", dynamicUrl);
            return dynamicUrl;
        } catch (UnknownHostException e) {
            log.error("Failed to detect IP address, falling back to localhost", e);
            return "http://localhost:" + serverPort;
        }
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        String serverUrl = appServerUrl(); // Call the method directly

        return new OpenAPI()
                .info(new Info()
                        .title("Loyalty Backend API")
                        .description("REST API for Loyalty Management System - manage users, events, companies, and vouchers")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Loyalty Team")
                                .email("support@loyalty.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Development Server URL"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                        ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication")));
    }
}