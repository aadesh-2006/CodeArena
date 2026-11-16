package com.codearena.common.controller;

import com.codearena.common.dto.ApiResponse;
import com.codearena.common.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application health and status endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check backend service health")
    public ResponseEntity<ApiResponse<HealthResponse>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.ok("Service is healthy", HealthResponse.up()));
    }
}
