package com.codearena.problem.dto;

public record TopicSummaryDto(
        Long id,
        String name,
        String slug,
        String description
) {}
