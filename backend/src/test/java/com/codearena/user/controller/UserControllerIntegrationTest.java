package com.codearena.user.controller;

import com.codearena.auth.security.JwtTokenService;
import com.codearena.user.dto.UpdateProfileRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/users/me - Should return 401 when unauthenticated")
    void getMe_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/me - Should return current user profile with valid JWT")
    void getMe_authenticated() throws Exception {
        User user = userRepository.save(new User(
                "profile_dev",
                "dev@codearena.dev",
                "Senior Dev",
                passwordEncoder.encode("Secret123!"),
                UserRole.USER
        ));

        String token = jwtTokenService.generateToken(user);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("profile_dev"))
                .andExpect(jsonPath("$.data.email").value("dev@codearena.dev"))
                .andExpect(jsonPath("$.data.displayName").value("Senior Dev"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/users/me - Should update username and display name")
    void updateMe_success() throws Exception {
        User user = userRepository.save(new User(
                "initial_name",
                "update@codearena.dev",
                "Initial Display",
                passwordEncoder.encode("Secret123!"),
                UserRole.USER
        ));

        String token = jwtTokenService.generateToken(user);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest("updated_name", "Updated Display Name");

        mockMvc.perform(put("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updated_name"))
                .andExpect(jsonPath("$.data.displayName").value("Updated Display Name"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        User persisted = userRepository.findById(user.getId()).orElseThrow();
        assertThat(persisted.getUsername()).isEqualTo("updated_name");
        assertThat(persisted.getDisplayName()).isEqualTo("Updated Display Name");
        assertThat(persisted.getRole()).isEqualTo(UserRole.USER); // Role cannot be modified
    }

    @Test
    @DisplayName("PUT /api/users/me - Should reject update when new username is taken")
    void updateMe_duplicateUsername() throws Exception {
        userRepository.save(new User("taken_name", "taken@codearena.dev", passwordEncoder.encode("Secret123!"), UserRole.USER));

        User user = userRepository.save(new User(
                "my_name",
                "mine@codearena.dev",
                "My Display",
                passwordEncoder.encode("Secret123!"),
                UserRole.USER
        ));

        String token = jwtTokenService.generateToken(user);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest("taken_name", "New Display");

        mockMvc.perform(put("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
