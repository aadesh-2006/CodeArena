package com.codearena.auth.bootstrap;

import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrapRunner runner;

    @Test
    @DisplayName("Should skip bootstrap when environment variables are empty")
    void run_skipsWhenEmpty() {
        runner.run(new DefaultApplicationArguments(new String[]{}));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should provision initial admin when credentials provided and no admin exists")
    void run_provisionsAdminSuccessfully() {
        ReflectionTestUtils.setField(runner, "adminUsername", "superadmin");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@codearena.dev");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecureAdminPass123!");
        ReflectionTestUtils.setField(runner, "adminDisplayName", "Platform SuperAdmin");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername("superadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@codearena.dev")).thenReturn(false);
        when(passwordEncoder.encode("SecureAdminPass123!")).thenReturn("bcrypt_admin_hash");

        runner.run(new DefaultApplicationArguments(new String[]{}));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("superadmin");
        assertThat(admin.getEmail()).isEqualTo("admin@codearena.dev");
        assertThat(admin.getDisplayName()).isEqualTo("Platform SuperAdmin");
        assertThat(admin.getPasswordHash()).isEqualTo("bcrypt_admin_hash");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should skip bootstrap if an admin already exists")
    void run_skipsWhenAdminAlreadyExists() {
        ReflectionTestUtils.setField(runner, "adminUsername", "superadmin");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@codearena.dev");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecureAdminPass123!");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip bootstrap when only some variables are provided (missing email)")
    void run_skipsWhenPartiallyConfigured() {
        ReflectionTestUtils.setField(runner, "adminUsername", "superadmin");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@codearena.dev");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecureAdminPass123!");
        ReflectionTestUtils.setField(runner, "adminEmail", "");

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not overwrite or promote existing ordinary user with matching username")
    void run_doesNotPromoteExistingOrdinaryUser() {
        ReflectionTestUtils.setField(runner, "adminUsername", "regular_user");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@codearena.dev");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecureAdminPass123!");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername("regular_user")).thenReturn(true);

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject admin provisioning if password length is under 8 characters")
    void run_rejectsShortPassword() {
        ReflectionTestUtils.setField(runner, "adminUsername", "superadmin");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@codearena.dev");
        ReflectionTestUtils.setField(runner, "adminPassword", "short");

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername("superadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@codearena.dev")).thenReturn(false);

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userRepository, never()).save(any());
    }
}
