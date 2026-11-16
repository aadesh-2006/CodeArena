package com.codearena.execution.contract.model;

public record ExecutionResponse(
        String stdout,
        String stderr,
        String compileOutput,
        Integer exitCode,
        Long timeMs,
        Long memoryKb,
        ExecutionVerdict verdict,
        String statusDescription
) {
    public static ExecutionResponse serviceUnavailable(String message) {
        return new ExecutionResponse(
                null,
                message,
                null,
                -1,
                0L,
                0L,
                ExecutionVerdict.INTERNAL_ERROR,
                "Execution service unavailable: " + message
        );
    }
}
