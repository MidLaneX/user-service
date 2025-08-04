package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Size(max = 1000, message = "Permissions cannot exceed 1000 characters")
    private String permissions;
}
