package com.codearena.auth.security;

import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    @DisplayName("Should generate valid signed JWT with correct identity and authority claims")
    void generateToken_shouldContainExpectedClaims() {
        User user = new User("jwt_subject", "jwt@codearena.dev", "hash", UserRole.USER);
        user.setId(42L);

        String token = jwtTokenService.generateToken(user);
        assertThat(token).isNotEmpty();

        Jwt decoded = jwtDecoder.decode(token);
        assertThat(decoded.getSubject()).isEqualTo("jwt_subject");
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("codearena");
        assertThat(decoded.getAudience()).contains("codearena-api");
        assertThat(decoded.getClaimAsString("email")).isEqualTo("jwt@codearena.dev");
        assertThat((Long) decoded.getClaim("userId")).isEqualTo(42L);
        assertThat(decoded.getClaimAsString("role")).isEqualTo("USER");
        assertThat(decoded.getClaimAsStringList("authorities")).contains("ROLE_USER");
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    @DisplayName("Should reject token when signature is altered or signed with different key")
    void decode_shouldRejectInvalidSignature() {
        User user = new User("victim", "victim@codearena.dev", "hash", UserRole.USER);
        user.setId(99L);
        String token = jwtTokenService.generateToken(user);

        // Tamper with the token payload
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + "tampered." + parts[2];

        assertThatThrownBy(() -> jwtDecoder.decode(tamperedToken))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    @DisplayName("Should reject token when issuer is untrusted")
    void decode_shouldRejectUntrustedIssuer() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("untrusted-issuer")
                .audience(List.of("codearena-api"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject("malicious_user")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        assertThatThrownBy(() -> jwtDecoder.decode(token))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    @DisplayName("Should reject token when audience is mismatched")
    void decode_shouldRejectMismatchedAudience() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("codearena")
                .audience(List.of("different-application"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject("user")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        assertThatThrownBy(() -> jwtDecoder.decode(token))
                .isInstanceOf(BadJwtException.class);
    }
}
