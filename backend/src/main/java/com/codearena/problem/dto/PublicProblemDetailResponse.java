package com.codearena.problem.dto;

import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemType;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record PublicProblemDetailResponse(
        String slug,
        String title,
        String statement,
        ProblemDifficulty difficulty,
        ProblemType problemType,
        String constraintsText,
        String inputFormat,
        String outputFormat,
        int timeLimitMs,
        int memoryLimitMb,
        String editorial,
        Set<TopicSummaryDto> topics,
        Set<TagSummaryDto> tags,
        List<ProblemExampleDto> examples,
        List<ProblemHintDto> hints,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PublicProblemDetailResponse fromEntity(Problem problem) {
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

        // Security rule: Only expose editorial content if explicitly published by admin
        String publicEditorial = problem.isEditorialPublished() ? problem.getEditorial() : null;

        return new PublicProblemDetailResponse(
                problem.getSlug(),
                problem.getTitle(),
                problem.getStatement(),
                problem.getDifficulty(),
                problem.getProblemType(),
                problem.getConstraintsText(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                publicEditorial,
                topicDtos,
                tagDtos,
                exampleDtos,
                hintDtos,
                problem.getCreatedAt(),
                problem.getUpdatedAt()
        );
    }
}
