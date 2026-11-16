package com.codearena.user.repository;

import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist and retrieve foundational user entity")
    void shouldPersistAndRetrieveUser() {
        User user = new User("architect", "lead@codearena.dev", "hashed_pwd_placeholder", UserRole.USER);
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isActive()).isTrue();

        Optional<User> found = userRepository.findByUsername("architect");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("lead@codearena.dev");
        assertThat(found.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should detect existence by username and email")
    void shouldCheckExistenceByUsernameAndEmail() {
        User user = new User("admin_test", "admin@codearena.dev", "hashed_pwd_placeholder", UserRole.ADMIN);
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("admin_test")).isTrue();
        assertThat(userRepository.existsByEmail("admin@codearena.dev")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }
}
