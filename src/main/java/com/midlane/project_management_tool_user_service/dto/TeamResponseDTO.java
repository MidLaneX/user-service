package com.midlane.project_management_tool_user_service.dto;

import com.midlane.project_management_tool_user_service.model.Team;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponseDTO(
        @io.swagger.v3.oas.annotations.media.Schema(description = "Team unique ID", example = "64b5f3a2e73c9d00123f4567")
        String id,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team name", example = "Development Team Alpha")
        String name,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team description")
        String description,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Department ID")
        String departmentId,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team lead user ID")
        String teamLeadId,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team lead name")
        String teamLeadName,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team type", example = "DEVELOPMENT")
        Team.TeamType type,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team status", example = "ACTIVE")
        Team.TeamStatus status,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Maximum members allowed", example = "10")
        int maxMembers,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Current member count", example = "8")
        int currentMemberCount,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Member IDs")
        List<String> memberIds,

        @io.swagger.v3.oas.annotations.media.Schema(description = "All project IDs")
        List<String> projectIds,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Team creation date")
        LocalDateTime createdAt,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Last update date")
        LocalDateTime updatedAt,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Created by user ID")
        String createdBy,

        @io.swagger.v3.oas.annotations.media.Schema(description = "Does the team have available slots?")
        boolean hasAvailableSlots
) {
}
