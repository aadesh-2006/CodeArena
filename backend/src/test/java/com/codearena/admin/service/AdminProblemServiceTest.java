package com.codearena.admin.service;

import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.problem.dto.*;
import com.codearena.problem.entity.*;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private AdminProblemService service;

    @Test
    @DisplayName("Should successfully create a problem in DRAFT status with topics, tags, examples, and hints")
    void createProblem_success() {
        CreateProblemRequest request = new CreateProblemRequest(
                "Two Sum",
                "two-sum",
                "Find two numbers that add up to target.",
                ProblemDifficulty.EASY,
                "2 <= nums.length <= 10^4",
                "Array nums, int target",
                "Indices [i, j]",
                "Use a hash map for linear lookup.",
                false,
                2000,
                256,
                ProblemType.ALGORITHMIC,
                Set.of(1L),
                Set.of(2L),
                List.of(new ProblemExampleDto(null, "[2,7,11,15], 9", "[0,1]", "Sum is 9", 1)),
                List.of(new ProblemHintDto(null, "Try hashing.", 1))
        );

        Topic topic = new Topic("Arrays", "arrays", "Arrays desc");
        topic.setId(1L);
        Tag tag = new Tag("Hash Table", "hash-table");
        tag.setId(2L);

        when(problemRepository.existsBySlug("two-sum")).thenReturn(false);
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        AdminProblemResponse response = service.createProblem(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.slug()).isEqualTo("two-sum");
        assertThat(response.status()).isEqualTo(ProblemStatus.DRAFT);
        assertThat(response.topics()).hasSize(1);
        assertThat(response.tags()).hasSize(1);
        assertThat(response.examples()).hasSize(1);
        assertThat(response.hints()).hasSize(1);
    }

    @Test
    @DisplayName("Should auto-generate slug from title when slug is not provided")
    void createProblem_autoGeneratesSlug() {
        CreateProblemRequest request = new CreateProblemRequest(
                "Valid Palindrome II",
                null,
                "Check if string can be palindrome after deleting at most one character.",
                ProblemDifficulty.EASY,
                "1 <= s.length <= 10^5",
                "String s",
                "Boolean true/false",
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(problemRepository.existsBySlug("valid-palindrome-ii")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setId(11L);
            return p;
        });

        AdminProblemResponse response = service.createProblem(request);

        assertThat(response.slug()).isEqualTo("valid-palindrome-ii");
    }

    @Test
    @DisplayName("Should reject creation when slug already exists")
    void createProblem_duplicateSlug() {
        CreateProblemRequest request = new CreateProblemRequest(
                "Duplicate Problem",
                "duplicate-slug",
                "Statement",
                ProblemDifficulty.MEDIUM,
                "Constraints",
                "In",
                "Out",
                null, false, null, null, null, null, null, null, null
        );

        when(problemRepository.existsBySlug("duplicate-slug")).thenReturn(true);

        assertThatThrownBy(() -> service.createProblem(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("duplicate-slug");

        verify(problemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject creation when topic ID does not exist")
    void createProblem_nonExistentTopic() {
        CreateProblemRequest request = new CreateProblemRequest(
                "Title", "title-slug", "Stmt", ProblemDifficulty.HARD, "Const", "In", "Out",
                null, false, null, null, null, Set.of(999L), null, null, null
        );

        when(problemRepository.existsBySlug("title-slug")).thenReturn(false);
        when(topicRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProblem(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Topic not found with id: 999");
    }

    @Test
    @DisplayName("Should successfully update problem and cleanly replace examples and hints")
    void updateProblem_success() {
        Problem existing = new Problem("old-slug", "Old Title", "Old Stmt", ProblemDifficulty.EASY, "Old Const", "Old In", "Old Out");
        existing.setId(5L);
        existing.addExample(new ProblemExample("old in", "old out", "old expl", 1));

        UpdateProblemRequest request = new UpdateProblemRequest(
                "New Title",
                "new-slug",
                "New Stmt",
                ProblemDifficulty.HARD,
                "New Const",
                "New In",
                "New Out",
                "New Editorial",
                true,
                3000,
                512,
                ProblemType.ALGORITHMIC,
                null,
                null,
                List.of(new ProblemExampleDto(null, "new in", "new out", "new expl", 1)),
                List.of(new ProblemHintDto(null, "new hint", 1))
        );

        when(problemRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(problemRepository.existsBySlug("new-slug")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminProblemResponse response = service.updateProblem(5L, request);

        assertThat(response.slug()).isEqualTo("new-slug");
        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.difficulty()).isEqualTo(ProblemDifficulty.HARD);
        assertThat(response.examples()).hasSize(1);
        assertThat(response.examples().get(0).inputText()).isEqualTo("new in");
        assertThat(response.hints()).hasSize(1);
    }

    @Test
    @DisplayName("Should successfully publish problem when all required fields and examples are present")
    void publishProblem_success() {
        Problem problem = new Problem("ready-slug", "Ready Title", "Statement", ProblemDifficulty.MEDIUM, "Const", "In", "Out");
        problem.setId(7L);
        problem.addExample(new ProblemExample("in", "out", "expl", 1));
        problem.setStatus(ProblemStatus.DRAFT);

        when(problemRepository.findById(7L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminProblemResponse response = service.publishProblem(7L);

        assertThat(response.status()).isEqualTo(ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should reject publishing problem when examples are missing")
    void publishProblem_failsWhenIncomplete() {
        Problem problem = new Problem("no-examples", "No Examples", "Statement", ProblemDifficulty.EASY, "Const", "In", "Out");
        problem.setId(8L);
        problem.setStatus(ProblemStatus.DRAFT);

        when(problemRepository.findById(8L)).thenReturn(Optional.of(problem));

        assertThatThrownBy(() -> service.publishProblem(8L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one example");

        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.DRAFT);
    }

    @Test
    @DisplayName("Should successfully archive problem without physically deleting it")
    void archiveProblem_success() {
        Problem problem = new Problem("to-archive", "Archived Problem", "Statement", ProblemDifficulty.EASY, "Const", "In", "Out");
        problem.setId(9L);
        problem.setStatus(ProblemStatus.PUBLISHED);

        when(problemRepository.findById(9L)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminProblemResponse response = service.archiveProblem(9L);

        assertThat(response.status()).isEqualTo(ProblemStatus.ARCHIVED);
    }
}
