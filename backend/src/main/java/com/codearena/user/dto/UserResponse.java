package com.codearena.user.dto;

import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        UserRole role,
        OffsetDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
