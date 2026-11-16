package com.codearena.execution.contract;

import com.codearena.execution.contract.model.ExecutionRequest;
import com.codearena.execution.contract.model.ExecutionResponse;

/**
 * Service contract defining the integration boundary between CodeArena
 * and an isolated code execution engine (e.g., Judge0 or container sandbox).
 *
 * Untrusted code MUST NEVER be executed inside the Spring Boot host process.
 * Actual execution engine integration will be connected in Milestone 5.
 */
public interface CodeExecutionService {

    /**
     * Executes the submitted source code against the provided input in an isolated sandbox.
     *
     * @param request the execution request containing code, language, stdin, and limits
     * @return the execution response with outputs, time, memory, and preliminary verdict
     */
    ExecutionResponse execute(ExecutionRequest request);

    /**
     * Checks whether the external execution service is healthy and available.
     *
     * @return true if the execution backend is reachable and ready to accept jobs
     */
    boolean isAvailable();
}
