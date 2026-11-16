package com.codearena.admin.service;

import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.problem.dto.*;
import com.codearena.problem.entity.*;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdminProblemService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;

    public AdminProblemService(ProblemRepository problemRepository,
                               TopicRepository topicRepository,
                               TagRepository tagRepository) {
        this.problemRepository = problemRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public AdminProblemResponse createProblem(CreateProblemRequest request) {
        String slug = resolveAndValidateSlug(request.slug(), request.title(), null);

        if (problemRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Problem with slug '" + slug + "' already exists");
        }

        Problem problem = new Problem(
                slug,
                request.title().trim(),
                request.statement(),
                request.difficulty(),
                request.constraintsText(),
                request.inputFormat(),
                request.outputFormat()
        );

        if (request.editorial() != null) {
            problem.setEditorial(request.editorial());
        }
        if (request.isEditorialPublished() != null) {
            problem.setEditorialPublished(request.isEditorialPublished());
        }
        if (request.timeLimitMs() != null) {
            problem.setTimeLimitMs(request.timeLimitMs());
        }
        if (request.memoryLimitMb() != null) {
            problem.setMemoryLimitMb(request.memoryLimitMb());
        }
        if (request.problemType() != null) {
            problem.setProblemType(request.problemType());
        }

        problem.setStatus(ProblemStatus.DRAFT);
        problem.setTopics(resolveTopics(request.topicIds()));
        problem.setTags(resolveTags(request.tagIds()));

        if (request.examples() != null) {
            for (ProblemExampleDto exDto : request.examples()) {
                problem.addExample(new ProblemExample(
                        exDto.inputText(),
                        exDto.outputText(),
                        exDto.explanation(),
                        exDto.orderIndex()
                ));
            }
        }

        if (request.hints() != null) {
            for (ProblemHintDto hintDto : request.hints()) {
                problem.addHint(new ProblemHint(
                        hintDto.hintText(),
                        hintDto.orderIndex()
                ));
            }
        }

        Problem saved = problemRepository.save(problem);
        return AdminProblemResponse.fromEntity(saved);
    }

    @Transactional
    public AdminProblemResponse updateProblem(Long id, UpdateProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        String newSlug = resolveAndValidateSlug(request.slug(), request.title(), problem.getSlug());
        if (!newSlug.equalsIgnoreCase(problem.getSlug())) {
            if (problemRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("Problem with slug '" + newSlug + "' already exists");
            }
            problem.setSlug(newSlug);
        }

        problem.setTitle(request.title().trim());
        problem.setStatement(request.statement());
        problem.setDifficulty(request.difficulty());
        problem.setConstraintsText(request.constraintsText());
        problem.setInputFormat(request.inputFormat());
        problem.setOutputFormat(request.outputFormat());

        if (request.editorial() != null) {
            problem.setEditorial(request.editorial());
        }
        if (request.isEditorialPublished() != null) {
            problem.setEditorialPublished(request.isEditorialPublished());
        }
        if (request.timeLimitMs() != null) {
            problem.setTimeLimitMs(request.timeLimitMs());
        }
        if (request.memoryLimitMb() != null) {
            problem.setMemoryLimitMb(request.memoryLimitMb());
        }
        if (request.problemType() != null) {
            problem.setProblemType(request.problemType());
        }

        problem.setTopics(resolveTopics(request.topicIds()));
        problem.setTags(resolveTags(request.tagIds()));

        // Update examples safely with clear & add
        List<ProblemExample> newExamples = new ArrayList<>();
        if (request.examples() != null) {
            for (ProblemExampleDto exDto : request.examples()) {
                newExamples.add(new ProblemExample(
                        exDto.inputText(),
                        exDto.outputText(),
                        exDto.explanation(),
                        exDto.orderIndex()
                ));
            }
        }
        problem.setExamples(newExamples);

        // Update hints safely with clear & add
        List<ProblemHint> newHints = new ArrayList<>();
        if (request.hints() != null) {
            for (ProblemHintDto hintDto : request.hints()) {
                newHints.add(new ProblemHint(
                        hintDto.hintText(),
                        hintDto.orderIndex()
                ));
            }
        }
        problem.setHints(newHints);

        // If already published, verify it did not become incomplete
        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            validateCompletenessForPublish(problem);
        }

        Problem updated = problemRepository.save(problem);
        return AdminProblemResponse.fromEntity(updated);
    }

    @Transactional
    public AdminProblemResponse publishProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        validateCompletenessForPublish(problem);

        problem.setStatus(ProblemStatus.PUBLISHED);
        Problem saved = problemRepository.save(problem);
        return AdminProblemResponse.fromEntity(saved);
    }

    @Transactional
    public AdminProblemResponse archiveProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problem.setStatus(ProblemStatus.ARCHIVED);
        Problem saved = problemRepository.save(problem);
        return AdminProblemResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AdminProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        // Initialize lazy collections within transactional boundary to prevent MultipleBagFetch / lazy init issues
        Hibernate.initialize(problem.getTopics());
        Hibernate.initialize(problem.getTags());
        Hibernate.initialize(problem.getExamples());
        Hibernate.initialize(problem.getHints());

        return AdminProblemResponse.fromEntity(problem);
    }

    private void validateCompletenessForPublish(Problem problem) {
        if (problem.getTitle() == null || problem.getTitle().isBlank()) {
            throw new IllegalArgumentException("Cannot publish problem without a valid title.");
        }
        if (problem.getStatement() == null || problem.getStatement().isBlank()) {
            throw new IllegalArgumentException("Cannot publish problem without a problem statement.");
        }
        if (problem.getDifficulty() == null) {
            throw new IllegalArgumentException("Cannot publish problem without specifying difficulty.");
        }
        if (problem.getConstraintsText() == null || problem.getConstraintsText().isBlank()) {
            throw new IllegalArgumentException("Cannot publish problem without constraints.");
        }
        if (problem.getInputFormat() == null || problem.getInputFormat().isBlank() ||
            problem.getOutputFormat() == null || problem.getOutputFormat().isBlank()) {
            throw new IllegalArgumentException("Cannot publish problem without input and output formats.");
        }
        if (problem.getExamples() == null || problem.getExamples().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish problem without at least one example test case.");
        }
    }

    private String resolveAndValidateSlug(String providedSlug, String title, String currentSlug) {
        String slug;
        if (providedSlug != null && !providedSlug.isBlank()) {
            slug = providedSlug.trim().toLowerCase();
        } else if (currentSlug != null && !currentSlug.isBlank()) {
            slug = currentSlug;
        } else {
            slug = generateSlugFromTitle(title);
        }

        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException("Slug '" + slug + "' is invalid. Must be lowercase alphanumeric characters separated by single hyphens.");
        }
        return slug;
    }

    private String generateSlugFromTitle(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (slug.isBlank()) {
            slug = "problem-" + System.currentTimeMillis();
        }
        return slug;
    }

    private Set<Topic> resolveTopics(Set<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Topic> topics = new HashSet<>();
        for (Long topicId : topicIds) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
            topics.add(topic);
        }
        return topics;
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));
            tags.add(tag);
        }
        return tags;
    }
}
