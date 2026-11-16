package com.codearena.auth.service;

import com.codearena.auth.dto.AuthResponse;
import com.codearena.auth.dto.LoginRequest;
import com.codearena.auth.dto.RegisterRequest;
import com.codearena.auth.security.JwtTokenService;
import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    @DisplayName("Should successfully register user with hashed password and default USER role")
    void register_success() {
        RegisterRequest request = new RegisterRequest("coder101", "coder@codearena.dev", "SecurePass1");

        when(userRepository.existsByUsername("coder101")).thenReturn(false);
        when(userRepository.existsByEmail("coder@codearena.dev")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass1")).thenReturn("hashed_bcrypt_secret");
        when(jwtTokenService.generateToken(any(User.class))).thenReturn("jwt_mock_token");
        when(jwtTokenService.getExpirationMs()).thenReturn(86400000L);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt_mock_token");
        assertThat(response.user().username()).isEqualTo("coder101");
        assertThat(response.user().email()).isEqualTo("coder@codearena.dev");
        assertThat(response.user().role()).isEqualTo(UserRole.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed_bcrypt_secret");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should reject registration when username is duplicate")
    void register_duplicateUsername() {
        RegisterRequest request = new RegisterRequest("existingUser", "new@codearena.dev", "SecurePass1");
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'existingUser' is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration when email is duplicate")
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest("newUser", "existing@codearena.dev", "SecurePass1");
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@codearena.dev")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'existing@codearena.dev' is already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully authenticate with valid username and password")
    void login_successWithUsername() {
        LoginRequest request = new LoginRequest("validUser", "Password123");
        User user = new User("validUser", "valid@codearena.dev", "hash", UserRole.USER);
        user.setId(5L);

        when(userRepository.findByUsernameOrEmail("validUser", "validuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(jwtTokenService.generateToken(user)).thenReturn("signed_token");
        when(jwtTokenService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("signed_token");
        assertThat(response.user().username()).isEqualTo("validUser");
    }

    @Test
    @DisplayName("Should successfully authenticate with valid email and password")
    void login_successWithEmail() {
        LoginRequest request = new LoginRequest("valid@codearena.dev", "Password123");
        User user = new User("validUser", "valid@codearena.dev", "hash", UserRole.USER);
        user.setId(5L);

        when(userRepository.findByUsernameOrEmail("valid@codearena.dev", "valid@codearena.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(jwtTokenService.generateToken(user)).thenReturn("signed_token");
        when(jwtTokenService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("signed_token");
        assertThat(response.user().email()).isEqualTo("valid@codearena.dev");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password does not match")
    void login_invalidPassword() {
        LoginRequest request = new LoginRequest("validUser", "WrongPassword");
        User user = new User("validUser", "valid@codearena.dev", "hash", UserRole.USER);

        when(userRepository.findByUsernameOrEmail("validUser", "validuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when user not found")
    void login_userNotFound() {
        LoginRequest request = new LoginRequest("unknownUser", "Password123");
        when(userRepository.findByUsernameOrEmail("unknownUser", "unknownuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");
    }
}
