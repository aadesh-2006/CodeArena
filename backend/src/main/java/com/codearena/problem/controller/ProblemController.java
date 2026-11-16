package com.codearena.problem.controller;

import com.codearena.common.dto.ApiResponse;
import com.codearena.common.dto.PageResponse;
import com.codearena.problem.dto.ProblemSummaryResponse;
import com.codearena.problem.dto.PublicProblemDetailResponse;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
@Tag(name = "Problem Discovery", description = "Public endpoints for browsing and reading published coding problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of published problems", description = "Supports filtering by search query, difficulty, topic slug, tag slug, and sorting")
    public ResponseEntity<ApiResponse<PageResponse<ProblemSummaryResponse>>> getProblems(
            @Parameter(description = "Zero-based page index (must be >= 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Case-insensitive title search keyword")
            @RequestParam(required = false) String search,
            @Parameter(description = "Difficulty filter (EASY, MEDIUM, HARD)")
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @Parameter(description = "Topic slug filter")
            @RequestParam(required = false) String topic,
            @Parameter(description = "Tag slug filter")
            @RequestParam(required = false) String tag,
            @Parameter(description = "Sort property (title, difficulty, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        PageResponse<ProblemSummaryResponse> response = problemService.getPublicProblems(
                page, size, search, difficulty, topic, tag, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponse.ok("Problems retrieved successfully", response));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get published problem details by slug", description = "Retrieves public problem description, examples, hints, and metadata. Returns 404 if draft or archived.")
    public ResponseEntity<ApiResponse<PublicProblemDetailResponse>> getProblemBySlug(
            @Parameter(description = "Problem unique slug")
            @PathVariable String slug
    ) {
        PublicProblemDetailResponse response = problemService.getPublicProblemBySlug(slug);
        return ResponseEntity.ok(ApiResponse.ok("Problem retrieved successfully", response));
    }
}
