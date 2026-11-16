package com.codearena.problem.dto;

import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ProblemSummaryResponse(
        Long id,
        String slug,
        String title,
        ProblemDifficulty difficulty,
        ProblemType problemType,
        Set<TopicSummaryDto> topics,
        Set<TagSummaryDto> tags,
        OffsetDateTime createdAt
) {
    public static ProblemSummaryResponse fromEntity(Problem problem) {
        Set<TopicSummaryDto> topicDtos = problem.getTopics().stream()
                .map(t -> new TopicSummaryDto(t.getId(), t.getName(), t.getSlug(), t.getDescription()))
                .collect(Collectors.toSet());

        Set<TagSummaryDto> tagDtos = problem.getTags().stream()
                .map(t -> new TagSummaryDto(t.getId(), t.getName(), t.getSlug()))
                .collect(Collectors.toSet());

        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getProblemType(),
                topicDtos,
                tagDtos,
                problem.getCreatedAt()
        );
    }
}
