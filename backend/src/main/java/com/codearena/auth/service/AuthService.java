package com.codearena.auth.service;

import com.codearena.auth.dto.AuthResponse;
import com.codearena.auth.dto.LoginRequest;
import com.codearena.auth.dto.RegisterRequest;
import com.codearena.auth.security.JwtTokenService;
import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.user.dto.UserResponse;
import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    // Pre-computed BCrypt hash (cost factor 12) used to equalize response timing when user is not found
    private static final String DUMMY_BCRYPT_HASH = "$2a$12$e8uq0ZlUeC7tGv5N2P4m2.eB.L0kGqZz5s8hF.7A5Gv3p2u9KzJ9y";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanUsername = request.username().trim();
        String cleanEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new DuplicateResourceException("Username '" + cleanUsername + "' is already taken");
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("Email '" + cleanEmail + "' is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                cleanUsername,
                cleanEmail,
                cleanUsername, // default displayName to username
                passwordHash,
                UserRole.USER
        );

        User savedUser = userRepository.save(user);
        String token = jwtTokenService.generateToken(savedUser);

        return AuthResponse.of(
                token,
                jwtTokenService.getExpirationMs(),
                UserResponse.fromEntity(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();

        User user = userRepository.findByUsernameOrEmail(identifier, identifier.toLowerCase())
                .orElse(null);

        if (user == null) {
            // Mitigate timing attack: compute matching time against dummy hash to prevent user enumeration
            passwordEncoder.matches(request.password(), DUMMY_BCRYPT_HASH);
            throw new BadCredentialsException("Invalid username/email or password");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled. Please contact support.");
        }

        String token = jwtTokenService.generateToken(user);

        return AuthResponse.of(
                token,
                jwtTokenService.getExpirationMs(),
                UserResponse.fromEntity(user)
        );
    }
}
