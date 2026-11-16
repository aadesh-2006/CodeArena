package com.codearena.execution.contract.model;

public enum ExecutionVerdict {
    PENDING,
    RUNNING,
    ACCEPTED,
    WRONG_ANSWER,
    TIME_LIMIT_EXCEEDED,
    RUNTIME_ERROR,
    COMPILATION_ERROR,
    INTERNAL_ERROR
}
