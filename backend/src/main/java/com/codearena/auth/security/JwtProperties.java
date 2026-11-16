package com.codearena.auth.security;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "codearena.jwt")
public class JwtProperties {

    public static final String DEFAULT_DEV_SECRET = "codearena_dev_secret_key_minimum_32_characters_for_signing_hmac_sha256!";

    /**
     * Secret key for HMAC-SHA256 signing. Must be at least 32 characters (256 bits).
     */
    private String secret = DEFAULT_DEV_SECRET;

    /**
     * Token expiration time in milliseconds (default: 24 hours).
     */
    private long expirationMs = 86400000L;

    private final Environment environment;

    public JwtProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateProperties() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT signing key must be at least 32 bytes (256 bits) for HMAC-SHA256 security.");
        }

        if (environment != null) {
            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
            boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");

            if (isProduction) {
                String normalized = secret.trim().toLowerCase();
                if (DEFAULT_DEV_SECRET.equalsIgnoreCase(secret)
                        || normalized.contains("dev_secret")
                        || normalized.contains("codearena_dev")
                        || "secret".equals(normalized)
                        || "changeme".equals(normalized)
                        || "password".equals(normalized)) {
                    throw new IllegalStateException(
                            "CRITICAL SECURITY CONFIGURATION ERROR: CodeArena is configured with an insecure or default JWT secret in production mode. " +
                            "You must supply a cryptographically strong, random 256-bit secret via the JWT_SECRET environment variable."
                    );
                }
            }
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
