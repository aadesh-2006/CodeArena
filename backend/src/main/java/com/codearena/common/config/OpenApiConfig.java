package com.codearena.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CodeArena API",
                version = "1.0.0",
                description = "Full-Stack DSA Practice Platform REST APIs",
                contact = @Contact(name = "CodeArena Team", email = "lead@codearena.dev")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT access token obtained from /api/auth/login or /api/auth/register"
)
public class OpenApiConfig {
}
