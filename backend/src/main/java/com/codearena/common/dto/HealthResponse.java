package com.codearena.common.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String service,
        String version,
        Instant timestamp
) {
    public static HealthResponse up() {
        return new HealthResponse("UP", "codearena-backend", "0.1.0-SNAPSHOT", Instant.now());
    }
}
