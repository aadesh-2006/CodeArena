package com.codearena.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record ProblemExampleDto(
        Long id,
        @NotBlank(message = "Example input cannot be blank")
        String inputText,
        @NotBlank(message = "Example output cannot be blank")
        String outputText,
        String explanation,
        int orderIndex
) {}
