package com.codearena.problem.service;

import com.codearena.common.dto.PageResponse;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.problem.dto.ProblemSummaryResponse;
import com.codearena.problem.dto.PublicProblemDetailResponse;
import com.codearena.problem.dto.TagSummaryDto;
import com.codearena.problem.dto.TopicSummaryDto;
import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemStatus;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import com.codearena.problem.specification.ProblemSpecification;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("title", "difficulty", "createdAt");

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;

    public ProblemService(ProblemRepository problemRepository,
                          TopicRepository topicRepository,
                          TagRepository tagRepository) {
        this.problemRepository = problemRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProblemSummaryResponse> getPublicProblems(
            int page,
            int size,
            String search,
            ProblemDifficulty difficulty,
            String topicSlug,
            String tagSlug,
            String sortBy,
            String sortDirection
    ) {
        validatePaginationParameters(page, size);
        String resolvedSortProperty = resolveAndValidateSortProperty(sortBy);
        Sort.Direction direction = resolveAndValidateSortDirection(sortDirection);

        // Deterministic secondary sort by id prevents pagination jitter
        Sort sort = Sort.by(direction, resolvedSortProperty).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Problem> spec = ProblemSpecification.buildPublicDiscoverySpec(
                search,
                difficulty,
                topicSlug,
                tagSlug
        );

        // Two-step pagination to eliminate N+1 queries and avoid Cartesian product issues
        Page<Problem> pageResult = problemRepository.findAll(spec, pageable);

        if (pageResult.isEmpty()) {
            return PageResponse.of(Collections.emptyList(), pageResult);
        }

        List<Long> pageIds = pageResult.getContent().stream().map(Problem::getId).toList();

        // Separate batch queries to eliminate SQL Cartesian multiplication (O(T + G) instead of O(T * G))
        // Query 2a loads topics onto the managed Problem entities in the persistence context
        List<Problem> problemsWithTopics = problemRepository.findWithTopicsByIdIn(pageIds);
        // Query 2b initializes tags onto the same managed entities in the persistence context
        problemRepository.findWithTagsByIdIn(pageIds);

        Map<Long, Problem> problemMap = problemsWithTopics.stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity(), (existing, replacement) -> existing));

        // Explicitly preserve the exact ordering of the initial page query
        List<ProblemSummaryResponse> dtoList = pageIds.stream()
                .map(problemMap::get)
                .filter(Objects::nonNull)
                .map(ProblemSummaryResponse::fromEntity)
                .toList();

        return PageResponse.of(dtoList, pageResult);
    }

    @Transactional(readOnly = true)
    public PublicProblemDetailResponse getPublicProblemBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResourceNotFoundException("Problem not found");
        }
        String cleanSlug = slug.trim().toLowerCase();

        // Strictly query for PUBLISHED problems only. DRAFT or ARCHIVED will return 404 without leaking status.
        // Split query eliminates Cartesian multiplication across topics, tags, examples, and hints.
        Problem problem = problemRepository.findWithTopicsBySlugAndStatus(cleanSlug, ProblemStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + cleanSlug));

        problemRepository.findWithTagsBySlugAndStatus(cleanSlug, ProblemStatus.PUBLISHED);

        Hibernate.initialize(problem.getExamples());
        Hibernate.initialize(problem.getHints());

        return PublicProblemDetailResponse.fromEntity(problem);
    }

    @Transactional(readOnly = true)
    public List<TopicSummaryDto> getDiscoveryTopics() {
        return topicRepository.findDistinctTopicsForPublishedProblems().stream()
                .map(t -> new TopicSummaryDto(t.getId(), t.getName(), t.getSlug(), t.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagSummaryDto> getDiscoveryTags() {
        return tagRepository.findDistinctTagsForPublishedProblems().stream()
                .map(t -> new TagSummaryDto(t.getId(), t.getName(), t.getSlug()))
                .toList();
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }
    }

    private String resolveAndValidateSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        String property = sortBy.trim();
        if ("created_at".equalsIgnoreCase(property)) {
            property = "createdAt";
        }
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new IllegalArgumentException(
                    "Invalid sort property '" + sortBy + "'. Allowed sort fields are: " + String.join(", ", ALLOWED_SORT_PROPERTIES)
            );
        }
        return property;
    }

    private Sort.Direction resolveAndValidateSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return Sort.Direction.DESC;
        }
        try {
            return Sort.Direction.fromString(sortDirection.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid sort direction '" + sortDirection + "'. Allowed values are: ASC, DESC.");
        }
    }
}
