package com.codearena.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtPropertiesSecurityTest {

    @Test
    @DisplayName("Should throw IllegalStateException when secret key length is under 32 bytes (256 bits)")
    void validate_rejectsShortKey() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtProperties properties = new JwtProperties(env);
        properties.setSecret("too_short_key_under_32_chars");

        assertThatThrownBy(properties::validateProperties)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("Should throw IllegalStateException in production profile if default dev secret is used")
    void validate_rejectsDefaultSecretInProduction() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        JwtProperties properties = new JwtProperties(env);
        properties.setSecret(JwtProperties.DEFAULT_DEV_SECRET);

        assertThatThrownBy(properties::validateProperties)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("insecure or default JWT secret in production mode");
    }

    @Test
    @DisplayName("Should pass validation in production profile when a cryptographically strong secret is configured")
    void validate_acceptsStrongSecretInProduction() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        JwtProperties properties = new JwtProperties(env);
        properties.setSecret("a_very_secure_high_entropy_random_256_bit_production_secret_key_12345!");

        assertThatCode(properties::validateProperties).doesNotThrowAnyException();
    }
}
