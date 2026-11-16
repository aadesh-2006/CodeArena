package com.codearena.problem.controller;

import com.codearena.common.dto.ApiResponse;
import com.codearena.problem.dto.TagSummaryDto;
import com.codearena.problem.dto.TopicSummaryDto;
import com.codearena.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Discovery Metadata", description = "Public endpoints for discovering topics and tags associated with published problems")
public class DiscoveryController {

    private final ProblemService problemService;

    public DiscoveryController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/api/topics")
    @Operation(summary = "Get published topics", description = "Returns distinct topics associated with at least one published problem")
    public ResponseEntity<ApiResponse<List<TopicSummaryDto>>> getTopics() {
        List<TopicSummaryDto> topics = problemService.getDiscoveryTopics();
        return ResponseEntity.ok(ApiResponse.ok("Topics retrieved successfully", topics));
    }

    @GetMapping("/api/tags")
    @Operation(summary = "Get published tags", description = "Returns distinct tags associated with at least one published problem")
    public ResponseEntity<ApiResponse<List<TagSummaryDto>>> getTags() {
        List<TagSummaryDto> tags = problemService.getDiscoveryTags();
        return ResponseEntity.ok(ApiResponse.ok("Tags retrieved successfully", tags));
    }
}
