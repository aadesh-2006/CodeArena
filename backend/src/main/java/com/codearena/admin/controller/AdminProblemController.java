package com.codearena.admin.controller;

import com.codearena.admin.service.AdminProblemService;
import com.codearena.common.dto.ApiResponse;
import com.codearena.problem.dto.AdminProblemResponse;
import com.codearena.problem.dto.CreateProblemRequest;
import com.codearena.problem.dto.UpdateProblemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/problems")
@Tag(name = "Admin Problem Management", description = "Endpoints for authoring, editing, publishing, and archiving problems (ADMIN role required)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProblemController {

    private final AdminProblemService adminProblemService;

    public AdminProblemController(AdminProblemService adminProblemService) {
        this.adminProblemService = adminProblemService;
    }

    @PostMapping
    @Operation(summary = "Create a new problem (Draft)", description = "Creates a problem with DRAFT status")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        AdminProblemResponse response = adminProblemService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Problem created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full administrative problem detail by ID")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> getProblemById(@PathVariable Long id) {
        AdminProblemResponse response = adminProblemService.getProblemById(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update problem details, examples, hints, and topics")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> updateProblem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProblemRequest request) {
        AdminProblemResponse response = adminProblemService.updateProblem(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Problem updated successfully", response));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a problem", description = "Validates problem completeness and transitions problem to PUBLISHED status")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> publishProblem(@PathVariable Long id) {
        AdminProblemResponse response = adminProblemService.publishProblem(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem published successfully", response));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a problem", description = "Transitions problem to ARCHIVED status without deleting historical records")
    public ResponseEntity<ApiResponse<AdminProblemResponse>> archiveProblem(@PathVariable Long id) {
        AdminProblemResponse response = adminProblemService.archiveProblem(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem archived successfully", response));
    }
}
