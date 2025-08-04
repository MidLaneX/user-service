package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AssignTeamToProjectRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;
}
