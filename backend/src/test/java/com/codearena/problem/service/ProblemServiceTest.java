package com.codearena.problem.service;

import com.codearena.common.dto.PageResponse;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.problem.dto.ProblemSummaryResponse;
import com.codearena.problem.dto.PublicProblemDetailResponse;
import com.codearena.problem.dto.TagSummaryDto;
import com.codearena.problem.dto.TopicSummaryDto;
import com.codearena.problem.entity.*;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ProblemService problemService;

    private Problem publishedProblem;

    @BeforeEach
    void setUp() {
        publishedProblem = new Problem(
                "two-sum",
                "Two Sum",
                "Given an array of integers nums and an integer target, return indices...",
                ProblemDifficulty.EASY,
                "2 <= nums.length <= 10^4",
                "Array nums and integer target",
                "Indices [i, j]"
        );
        publishedProblem.setEditorial("Use a hash map for linear time lookup.");
        publishedProblem.setEditorialPublished(true);
        publishedProblem.setTimeLimitMs(2000);
        publishedProblem.setMemoryLimitMb(256);
        publishedProblem.setProblemType(ProblemType.ALGORITHMIC);
        publishedProblem.setStatus(ProblemStatus.PUBLISHED);
        try {
            var idField = Problem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(publishedProblem, 100L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getPublicProblems - returns paginated summary when problems exist")
    void getPublicProblems_success() {
        Topic topic = new Topic("Arrays", "arrays", "Arrays desc");
        Tag tag = new Tag("Hash Table", "hash-table");
        publishedProblem.setTopics(Set.of(topic));
        publishedProblem.setTags(Set.of(tag));

        Page<Problem> pageResult = new PageImpl<>(List.of(publishedProblem));
        when(problemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);
        when(problemRepository.findWithTopicsByIdIn(List.of(100L)))
                .thenReturn(List.of(publishedProblem));
        when(problemRepository.findWithTagsByIdIn(List.of(100L)))
                .thenReturn(List.of(publishedProblem));

        PageResponse<ProblemSummaryResponse> response = problemService.getPublicProblems(
                0, 20, "Two", ProblemDifficulty.EASY, "arrays", "hash-table", "createdAt", "DESC"
        );

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).slug()).isEqualTo("two-sum");
        assertThat(response.content().get(0).topics()).hasSize(1);
        assertThat(response.content().get(0).tags()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPublicProblems - returns empty page when no matches")
    void getPublicProblems_empty() {
        Page<Problem> emptyPage = new PageImpl<>(Collections.emptyList());
        when(problemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        PageResponse<ProblemSummaryResponse> response = problemService.getPublicProblems(
                0, 20, "NonExistent", null, null, null, null, null
        );

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        verify(problemRepository, never()).findWithTopicsByIdIn(any());
        verify(problemRepository, never()).findWithTagsByIdIn(any());
    }

    @Test
    @DisplayName("getPublicProblems - throws on invalid pagination arguments")
    void getPublicProblems_invalidPagination() {
        assertThatThrownBy(() -> problemService.getPublicProblems(-1, 20, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page index must not be less than zero");

        assertThatThrownBy(() -> problemService.getPublicProblems(0, 0, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be between 1 and 100");

        assertThatThrownBy(() -> problemService.getPublicProblems(0, 101, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be between 1 and 100");
    }

    @Test
    @DisplayName("getPublicProblems - throws on invalid sortBy property")
    void getPublicProblems_invalidSort() {
        assertThatThrownBy(() -> problemService.getPublicProblems(0, 20, null, null, null, null, "secretScore", "ASC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort property 'secretScore'");
    }

    @Test
    @DisplayName("getPublicProblems - throws on invalid sortDirection")
    void getPublicProblems_invalidSortDirection() {
        assertThatThrownBy(() -> problemService.getPublicProblems(0, 20, null, null, null, null, "title", "DIAGONAL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort direction 'DIAGONAL'");
    }

    @Test
    @DisplayName("getPublicProblemBySlug - returns public detail when problem is PUBLISHED")
    void getPublicProblemBySlug_success() {
        when(problemRepository.findWithTopicsBySlugAndStatus("two-sum", ProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(publishedProblem));

        PublicProblemDetailResponse detail = problemService.getPublicProblemBySlug("two-sum");

        assertThat(detail).isNotNull();
        assertThat(detail.slug()).isEqualTo("two-sum");
        assertThat(detail.editorial()).isEqualTo("Use a hash map for linear time lookup.");
        verify(problemRepository).findWithTagsBySlugAndStatus("two-sum", ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getPublicProblemBySlug - hides editorial content when editorialPublished is false")
    void getPublicProblemBySlug_editorialHiddenWhenUnpublished() {
        publishedProblem.setEditorialPublished(false);
        when(problemRepository.findWithTopicsBySlugAndStatus("two-sum", ProblemStatus.PUBLISHED))
                .thenReturn(Optional.of(publishedProblem));

        PublicProblemDetailResponse detail = problemService.getPublicProblemBySlug("two-sum");

        assertThat(detail).isNotNull();
        assertThat(detail.editorial()).isNull();
        verify(problemRepository).findWithTagsBySlugAndStatus("two-sum", ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("getPublicProblemBySlug - throws ResourceNotFoundException if problem not found or not PUBLISHED")
    void getPublicProblemBySlug_notFound() {
        when(problemRepository.findWithTopicsBySlugAndStatus("draft-slug", ProblemStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getPublicProblemBySlug("draft-slug"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    @DisplayName("getDiscoveryTopics - returns distinct topics for published problems")
    void getDiscoveryTopics_success() {
        Topic topic = new Topic("Dynamic Programming", "dp", "DP algorithms");
        when(topicRepository.findDistinctTopicsForPublishedProblems())
                .thenReturn(List.of(topic));

        List<TopicSummaryDto> topics = problemService.getDiscoveryTopics();

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).slug()).isEqualTo("dp");
    }

    @Test
    @DisplayName("getDiscoveryTags - returns distinct tags for published problems")
    void getDiscoveryTags_success() {
        Tag tag = new Tag("Recursion", "recursion");
        when(tagRepository.findDistinctTagsForPublishedProblems())
                .thenReturn(List.of(tag));

        List<TagSummaryDto> tags = problemService.getDiscoveryTags();

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).slug()).isEqualTo("recursion");
    }

    @Test
    @DisplayName("getPublicProblems - preserves pagination sort order even if batch query returns entities in different order")
    void getPublicProblems_preservesOrder() {
        Problem problemAlpha = new Problem("alpha", "Alpha", "...", ProblemDifficulty.EASY, "c", "i", "o");
        problemAlpha.setStatus(ProblemStatus.PUBLISHED);
        Problem problemZeta = new Problem("zeta", "Zeta", "...", ProblemDifficulty.EASY, "c", "i", "o");
        problemZeta.setStatus(ProblemStatus.PUBLISHED);

        try {
            var idField = Problem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(problemAlpha, 20L);
            idField.set(problemZeta, 10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Page<Problem> pageResult = new PageImpl<>(List.of(problemAlpha, problemZeta));
        when(problemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageResult);

        // Database returns entities ordered by primary key [10, 20] instead of requested sort order [20, 10]
        when(problemRepository.findWithTopicsByIdIn(List.of(20L, 10L)))
                .thenReturn(List.of(problemZeta, problemAlpha));
        when(problemRepository.findWithTagsByIdIn(List.of(20L, 10L)))
                .thenReturn(List.of(problemZeta, problemAlpha));

        PageResponse<ProblemSummaryResponse> response = problemService.getPublicProblems(
                0, 20, null, null, null, null, "title", "ASC"
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).slug()).isEqualTo("alpha");
        assertThat(response.content().get(1).slug()).isEqualTo("zeta");
    }
}
