package com.codearena.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record ProblemHintDto(
        Long id,
        @NotBlank(message = "Hint text cannot be blank")
        String hintText,
        int orderIndex
) {}
