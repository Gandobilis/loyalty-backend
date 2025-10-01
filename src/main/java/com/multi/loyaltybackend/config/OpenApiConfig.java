package com.multi.loyaltybackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Loyalty Service API",
                version = "1.0.0",
                description = "This API provides endpoints for managing customer loyalty programs, points, and rewards.",
                contact = @Contact(
                        name = "API Support",
                        url = "https://your-company.com/support",
                        email = "tech@your-company.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Development Server"
                ),
                @Server(
                        url = "https://api.your-company.com",
                        description = "Production Server"
                )
        }
)
public class OpenApiConfig { }