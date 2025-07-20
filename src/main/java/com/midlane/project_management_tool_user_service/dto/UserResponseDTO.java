package com.midlane.project_management_tool_user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
        @Schema(description = "User unique ID", example = "64b5f3a2e73c9d00123f4567")
        String id,

        @Schema(description = "User full name", example = "John Doe")
        String name,

        @Schema(description = "User email address", example = "john@example.com")
        String email,

        @Schema(description = "User role", example = "ADMIN")
        String role
) {}
