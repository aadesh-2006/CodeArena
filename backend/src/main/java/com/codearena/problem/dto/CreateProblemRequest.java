package com.codearena.problem.dto;

import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public record CreateProblemRequest(
        @NotBlank(message = "Problem title cannot be blank")
        @Size(max = 200, message = "Problem title cannot exceed 200 characters")
        String title,

        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens (e.g. 'two-sum')")
        @Size(max = 100, message = "Slug cannot exceed 100 characters")
        String slug,

        @NotBlank(message = "Problem statement cannot be blank")
        String statement,

        @NotNull(message = "Difficulty is required")
        ProblemDifficulty difficulty,

        @NotBlank(message = "Constraints cannot be blank")
        String constraintsText,

        @NotBlank(message = "Input format cannot be blank")
        String inputFormat,

        @NotBlank(message = "Output format cannot be blank")
        String outputFormat,

        String editorial,

        Boolean isEditorialPublished,

        @Min(value = 100, message = "Time limit must be at least 100 ms")
        @Max(value = 15000, message = "Time limit cannot exceed 15000 ms")
        Integer timeLimitMs,

        @Min(value = 16, message = "Memory limit must be at least 16 MB")
        @Max(value = 2048, message = "Memory limit cannot exceed 2048 MB")
        Integer memoryLimitMb,

        ProblemType problemType,

        Set<Long> topicIds,

        Set<Long> tagIds,

        List<@Valid ProblemExampleDto> examples,

        List<@Valid ProblemHintDto> hints
) {}
