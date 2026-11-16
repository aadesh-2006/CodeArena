package com.codearena.auth.controller;

import com.codearena.auth.dto.LoginRequest;
import com.codearena.auth.dto.RegisterRequest;
import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register new user and return token and profile")
    void register_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest("alice_coder", "alice@codearena.dev", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("alice_coder"))
                .andExpect(jsonPath("$.data.user.email").value("alice@codearena.dev"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist());

        User persisted = userRepository.findByUsername("alice_coder").orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", persisted.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail when username already exists")
    void register_shouldRejectDuplicateUsername() throws Exception {
        userRepository.save(new User("existing_user", "old@codearena.dev", passwordEncoder.encode("Password123!"), UserRole.USER));

        RegisterRequest request = new RegisterRequest("existing_user", "unique@codearena.dev", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail when email already exists")
    void register_shouldRejectDuplicateEmail() throws Exception {
        userRepository.save(new User("unique1", "duplicate@codearena.dev", passwordEncoder.encode("Password123!"), UserRole.USER));

        RegisterRequest request = new RegisterRequest("unique2", "duplicate@codearena.dev", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should reject invalid email format")
    void register_shouldRejectInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("validuser", "not-an-email", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should reject weak password")
    void register_shouldRejectWeakPassword() throws Exception {
        // Less than 8 characters, or no digits
        RegisterRequest request = new RegisterRequest("validuser", "valid@codearena.dev", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should successfully authenticate and return token")
    void login_shouldSucceed() throws Exception {
        userRepository.save(new User("login_user", "login@codearena.dev", passwordEncoder.encode("Password123!"), UserRole.USER));

        LoginRequest request = new LoginRequest("login_user", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("login_user"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should reject invalid password with 401")
    void login_shouldRejectInvalidPassword() throws Exception {
        userRepository.save(new User("login_user", "login@codearena.dev", passwordEncoder.encode("Password123!"), UserRole.USER));

        LoginRequest request = new LoginRequest("login_user", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should reject non-existent user with 401 without leaking user existence")
    void login_shouldRejectUnknownUser() throws Exception {
        LoginRequest request = new LoginRequest("ghost_user", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }
}
