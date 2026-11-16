package com.codearena.execution.contract.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExecutionRequest(
        @NotBlank(message = "Source code cannot be blank")
        String sourceCode,

        @NotNull(message = "Language is required")
        SupportedLanguage language,

        String stdin,

        Integer timeLimitMs,

        Integer memoryLimitKb
) {
    public static ExecutionRequest of(String sourceCode, SupportedLanguage language, String stdin) {
        return new ExecutionRequest(sourceCode, language, stdin, 2000, 128000);
    }
}
