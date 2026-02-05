package com.supermarket.supermarket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Supermarket API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Supermarket Support",
                        email = "support@supermarket.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "<https://www.apache.org/licenses/LICENSE-2.0>"
                ),
                description = "RESTful API for Supermarket Management System with JWT Authentication"
        ),
        servers = @Server(
                url = "<http://localhost:8080>",
                description = "Local Server"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "JWT authentication with Bearer token",
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.HTTP
)
@Configuration
public class OpenAPIConfig {
}