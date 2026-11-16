package com.codearena.problem.repository;

import com.codearena.problem.entity.Topic;
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
class TopicRepositoryTest {

    @Autowired
    private TopicRepository topicRepository;

    @BeforeEach
    void setUp() {
        topicRepository.deleteAll();
        topicRepository.flush();
    }

    @Test
    @DisplayName("Should save and retrieve topic by slug and name")
    void saveAndFind() {
        Topic topic = new Topic("Dynamic Programming", "dynamic-programming", "DP techniques and subproblems");
        topicRepository.save(topic);

        Optional<Topic> bySlug = topicRepository.findBySlug("dynamic-programming");
        assertThat(bySlug).isPresent();
        assertThat(bySlug.get().getName()).isEqualTo("Dynamic Programming");

        Optional<Topic> byName = topicRepository.findByName("Dynamic Programming");
        assertThat(byName).isPresent();
    }

    @Test
    @DisplayName("Should reject duplicate topic name or slug")
    void rejectDuplicates() {
        topicRepository.saveAndFlush(new Topic("Trees", "trees", "Binary and n-ary trees"));

        assertThatThrownBy(() -> topicRepository.saveAndFlush(new Topic("Trees", "trees-2", "Desc")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> topicRepository.saveAndFlush(new Topic("Binary Trees", "trees", "Desc")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
