package com.codearena.problem.dto;

import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemStatus;
import com.codearena.problem.entity.ProblemType;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminProblemResponse(
        Long id,
        String slug,
        String title,
        String statement,
        ProblemDifficulty difficulty,
        String constraintsText,
        String inputFormat,
        String outputFormat,
        String editorial,
        boolean editorialPublished,
        int timeLimitMs,
        int memoryLimitMb,
        ProblemStatus status,
        ProblemType problemType,
        Set<TopicSummaryDto> topics,
        Set<TagSummaryDto> tags,
        List<ProblemExampleDto> examples,
        List<ProblemHintDto> hints,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static AdminProblemResponse fromEntity(Problem problem) {
        Set<TopicSummaryDto> topicDtos = problem.getTopics().stream()
                .map(t -> new TopicSummaryDto(t.getId(), t.getName(), t.getSlug(), t.getDescription()))
                .collect(Collectors.toSet());

        Set<TagSummaryDto> tagDtos = problem.getTags().stream()
                .map(t -> new TagSummaryDto(t.getId(), t.getName(), t.getSlug()))
                .collect(Collectors.toSet());

        List<ProblemExampleDto> exampleDtos = problem.getExamples().stream()
                .sorted(Comparator.comparingInt(com.codearena.problem.entity.ProblemExample::getOrderIndex))
                .map(e -> new ProblemExampleDto(e.getId(), e.getInputText(), e.getOutputText(), e.getExplanation(), e.getOrderIndex()))
                .toList();

        List<ProblemHintDto> hintDtos = problem.getHints().stream()
                .sorted(Comparator.comparingInt(com.codearena.problem.entity.ProblemHint::getOrderIndex))
                .map(h -> new ProblemHintDto(h.getId(), h.getHintText(), h.getOrderIndex()))
                .toList();

        return new AdminProblemResponse(
                problem.getId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getStatement(),
                problem.getDifficulty(),
                problem.getConstraintsText(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getEditorial(),
                problem.isEditorialPublished(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.getStatus(),
                problem.getProblemType(),
                topicDtos,
                tagDtos,
                exampleDtos,
                hintDtos,
                problem.getCreatedAt(),
                problem.getUpdatedAt()
        );
    }
}
