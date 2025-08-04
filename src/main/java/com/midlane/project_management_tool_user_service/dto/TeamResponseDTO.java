package com.midlane.project_management_tool_user_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponseDTO(
        @io.swagger.v3.oas.annotations.media.Schema(description = "Team unique ID", example = "64b5f3a2e73c9d00123f4567")
        Long id,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team name", example = "Development Team Alpha")
        String name,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team description")
        String description,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team lead user ID")
        String teamLeadId,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Managed team ID")
        String managedTeamId,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team type", example = "DEVELOPMENT")
        String type,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team status", example = "ACTIVE")
        String status,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Maximum members allowed", example = "10")
        int maxMembers,


        @io.swagger.v3.oas.annotations.media.Schema(description = "Member IDs")
        List<String> memberIds,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Created by user ID")
        String createdBy,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team creation date")
        LocalDateTime createdAt,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Last update date")
        LocalDateTime updatedAt
) {
}
