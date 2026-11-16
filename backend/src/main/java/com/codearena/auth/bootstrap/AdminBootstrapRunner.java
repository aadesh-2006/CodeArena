package com.codearena.auth.bootstrap;

import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Secure, environment-configured initial administrator bootstrapping runner.
 * <p>
 * Operates during application startup without exposing any public HTTP endpoints.
 * Credentials must be provided via environment variables:
 * - CODEARENA_ADMIN_USERNAME (or codearena.admin.username)
 * - CODEARENA_ADMIN_EMAIL (or codearena.admin.email)
 * - CODEARENA_ADMIN_PASSWORD (or codearena.admin.password)
 * - CODEARENA_ADMIN_DISPLAY_NAME (or codearena.admin.display-name)
 * <p>
 * If any required credential is missing or empty, bootstrap gracefully skips.
 * If an admin user already exists, or the username/email is already taken, bootstrap is skipped (idempotent).
 * Passwords and sensitive data are strictly NEVER logged.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${codearena.admin.username:}")
    private String adminUsername;

    @Value("${codearena.admin.email:}")
    private String adminEmail;

    @Value("${codearena.admin.password:}")
    private String adminPassword;

    @Value("${codearena.admin.display-name:Administrator}")
    private String adminDisplayName;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean hasUsername = adminUsername != null && !adminUsername.isBlank();
        boolean hasEmail = adminEmail != null && !adminEmail.isBlank();
        boolean hasPassword = adminPassword != null && !adminPassword.isBlank();

        if (!hasUsername && !hasEmail && !hasPassword) {
            log.debug("No initial admin credentials provided via environment configuration. Skipping admin bootstrap.");
            return;
        }

        if (!hasUsername || !hasEmail || !hasPassword) {
            log.warn("Incomplete admin bootstrap configuration: username, email, and password must all be supplied. Skipping admin bootstrap.");
            return;
        }

        String username = adminUsername.trim();
        String email = adminEmail.trim().toLowerCase();

        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.info("Administrator account already exists. Skipping initial admin bootstrap.");
            return;
        }

        if (userRepository.existsByUsername(username)) {
            log.warn("Cannot bootstrap admin account: Username '{}' already exists as a non-admin user.", username);
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Cannot bootstrap admin account: Email '{}' is already in use.", email);
            return;
        }

        if (adminPassword.length() < 8) {
            log.error("Cannot bootstrap admin account: Admin password must meet minimum length of 8 characters.");
            return;
        }

        String displayName = (adminDisplayName != null && !adminDisplayName.isBlank())
                ? adminDisplayName.trim()
                : "Administrator";

        User adminUser = new User(
                username,
                email,
                displayName,
                passwordEncoder.encode(adminPassword),
                UserRole.ADMIN
        );

        userRepository.save(adminUser);
        log.info("Initial administrator account successfully provisioned: username='{}', email='{}'.", username, email);
    }
}
