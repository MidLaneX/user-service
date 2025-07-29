package com.midlane.project_management_tool_user_service.dto;

import com.midlane.project_management_tool_user_service.model.User;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO(
        String id,
        String email,
        String username,
        String firstName,
        String lastName,
        String fullName,
        String phoneNumber,
        String profilePictureUrl,
        String jobTitle,
        String department,
        List<String> teamIds,
        String managedTeamId,
        User.UserStatus status,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt
) {
}
