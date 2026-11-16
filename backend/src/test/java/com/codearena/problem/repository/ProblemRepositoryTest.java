package com.codearena.problem.repository;

import com.codearena.problem.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TagRepository tagRepository;

    private Topic savedTopic;
    private Tag savedTag;

    @BeforeEach
    void setUp() {
        problemRepository.deleteAll();
        topicRepository.deleteAll();
        tagRepository.deleteAll();
        problemRepository.flush();
        topicRepository.flush();
        tagRepository.flush();

        savedTopic = topicRepository.save(new Topic("Arrays", "arrays", "Array data structure challenges"));
        savedTag = tagRepository.save(new Tag("Hash Table", "hash-table"));
    }

    @Test
    @DisplayName("Should successfully persist problem with topics, tags, examples, and hints")
    void saveProblem_shouldPersistRelationships() {
        Problem problem = new Problem(
                "two-sum",
                "Two Sum",
                "Given an array of integers, return indices of the two numbers.",
                ProblemDifficulty.EASY,
                "2 <= nums.length <= 10^4",
                "Array nums and integer target",
                "Indices [i, j]"
        );
        problem.setStatus(ProblemStatus.PUBLISHED);
        problem.setTopics(Set.of(savedTopic));
        problem.setTags(Set.of(savedTag));

        ProblemExample example1 = new ProblemExample("nums = [2,7,11,15], target = 9", "[0,1]", "Because nums[0] + nums[1] == 9", 1);
        problem.addExample(example1);

        ProblemHint hint1 = new ProblemHint("Try using a hash map for O(n) lookup.", 1);
        problem.addHint(hint1);

        Problem saved = problemRepository.save(problem);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<Problem> retrieved = problemRepository.findWithTopicsAndTagsBySlug("two-sum");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Two Sum");
        assertThat(retrieved.get().getDifficulty()).isEqualTo(ProblemDifficulty.EASY);
        assertThat(retrieved.get().getTopics()).hasSize(1);
        assertThat(retrieved.get().getTags()).hasSize(1);
        assertThat(retrieved.get().getExamples()).hasSize(1);
        assertThat(retrieved.get().getHints()).hasSize(1);
    }

    @Test
    @DisplayName("Should enforce unique slug constraint on problems")
    void saveProblem_duplicateSlugThrowsException() {
        Problem p1 = new Problem("unique-slug", "Title 1", "Stmt", ProblemDifficulty.EASY, "Const", "In", "Out");
        problemRepository.saveAndFlush(p1);

        Problem p2 = new Problem("unique-slug", "Title 2", "Stmt", ProblemDifficulty.HARD, "Const", "In", "Out");
        assertThatThrownBy(() -> problemRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should correctly filter problems by status and difficulty with pagination")
    void findByStatusAndDifficulty_shouldPageCorrectly() {
        Problem pubEasy = new Problem("pub-easy", "Pub Easy", "S", ProblemDifficulty.EASY, "C", "I", "O");
        pubEasy.setStatus(ProblemStatus.PUBLISHED);
        problemRepository.save(pubEasy);

        Problem pubMed = new Problem("pub-med", "Pub Med", "S", ProblemDifficulty.MEDIUM, "C", "I", "O");
        pubMed.setStatus(ProblemStatus.PUBLISHED);
        problemRepository.save(pubMed);

        Problem draftEasy = new Problem("draft-easy", "Draft Easy", "S", ProblemDifficulty.EASY, "C", "I", "O");
        draftEasy.setStatus(ProblemStatus.DRAFT);
        problemRepository.save(draftEasy);

        Page<Problem> publishedEasy = problemRepository.findByStatusAndDifficulty(
                ProblemStatus.PUBLISHED,
                ProblemDifficulty.EASY,
                PageRequest.of(0, 10)
        );

        assertThat(publishedEasy.getTotalElements()).isEqualTo(1);
        assertThat(publishedEasy.getContent().get(0).getSlug()).isEqualTo("pub-easy");

        Page<Problem> allPublished = problemRepository.findByStatus(ProblemStatus.PUBLISHED, PageRequest.of(0, 10));
        assertThat(allPublished.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find only published problems with findWithTopicsAndTagsBySlugAndStatus")
    void findWithTopicsAndTagsBySlugAndStatus_shouldRespectStatus() {
        Problem draft = new Problem("hidden-draft", "Draft Title", "S", ProblemDifficulty.HARD, "C", "I", "O");
        draft.setStatus(ProblemStatus.DRAFT);
        problemRepository.save(draft);

        Optional<Problem> foundPub = problemRepository.findWithTopicsAndTagsBySlugAndStatus("hidden-draft", ProblemStatus.PUBLISHED);
        assertThat(foundPub).isEmpty();

        Optional<Problem> foundDraft = problemRepository.findWithTopicsAndTagsBySlugAndStatus("hidden-draft", ProblemStatus.DRAFT);
        assertThat(foundDraft).isPresent();
    }

    @Test
    @DisplayName("Should fetch topics and tags independently via split-fetch queries without Cartesian multiplication")
    void splitFetchQueries_shouldLoadTopicsAndTagsSeparately() {
        Problem problem = new Problem("split-test", "Split Test", "Statement", ProblemDifficulty.MEDIUM, "C", "I", "O");
        problem.setStatus(ProblemStatus.PUBLISHED);
        problem.setTopics(Set.of(savedTopic));
        problem.setTags(Set.of(savedTag));
        Problem saved = problemRepository.save(problem);

        List<Problem> withTopics = problemRepository.findWithTopicsByIdIn(List.of(saved.getId()));
        assertThat(withTopics).hasSize(1);
        assertThat(withTopics.get(0).getTopics()).hasSize(1);

        List<Problem> withTags = problemRepository.findWithTagsByIdIn(List.of(saved.getId()));
        assertThat(withTags).hasSize(1);
        assertThat(withTags.get(0).getTags()).hasSize(1);

        Optional<Problem> slugWithTopics = problemRepository.findWithTopicsBySlugAndStatus("split-test", ProblemStatus.PUBLISHED);
        assertThat(slugWithTopics).isPresent();
        assertThat(slugWithTopics.get().getTopics()).hasSize(1);

        Optional<Problem> slugWithTags = problemRepository.findWithTagsBySlugAndStatus("split-test", ProblemStatus.PUBLISHED);
        assertThat(slugWithTags).isPresent();
        assertThat(slugWithTags.get().getTags()).hasSize(1);
    }
}
