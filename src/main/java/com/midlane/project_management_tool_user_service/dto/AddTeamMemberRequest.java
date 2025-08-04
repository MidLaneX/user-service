package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AddTeamMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
