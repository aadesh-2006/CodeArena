package com.codearena.problem.repository;

import com.codearena.problem.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
        tagRepository.flush();
    }

    @Test
    @DisplayName("Should save and retrieve tag by slug and name")
    void saveAndFind() {
        Tag tag = new Tag("Two Pointers", "two-pointers");
        tagRepository.save(tag);

        Optional<Tag> bySlug = tagRepository.findBySlug("two-pointers");
        assertThat(bySlug).isPresent();
        assertThat(bySlug.get().getName()).isEqualTo("Two Pointers");

        Optional<Tag> byName = tagRepository.findByName("Two Pointers");
        assertThat(byName).isPresent();
    }

    @Test
    @DisplayName("Should reject duplicate tag name or slug")
    void rejectDuplicates() {
        tagRepository.saveAndFlush(new Tag("Greedy", "greedy"));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(new Tag("Greedy", "greedy-2")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> tagRepository.saveAndFlush(new Tag("Greedy Algorithm", "greedy")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
