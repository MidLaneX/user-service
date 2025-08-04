package com.midlane.project_management_tool_user_service.dto;

import com.midlane.project_management_tool_user_service.model.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TeamRequestDTO(
        @NotBlank(message = "Team name is required")
        @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String teamLeadId,

        @NotNull(message = "Team type is required")
        String Type,

        @Positive(message = "Max members must be positive")
        int maxMembers,

        List<String> memberIds,

        List<String> projectIds,

        String status
) {
        public String type() {
                return type();
        }
}
