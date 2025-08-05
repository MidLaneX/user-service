package com.midlane.project_management_tool_user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "First Name is required")
        @Size(min = 3, max = 50, message = "First Name must be between 3 and 50 characters")
        String firstName,

        @NotBlank(message = "Last Name is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String lastName
) {}
