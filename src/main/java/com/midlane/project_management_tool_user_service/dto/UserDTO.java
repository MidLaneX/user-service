package com.midlane.project_management_tool_user_service.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserDTO(
        @Schema(description = "User unique ID", example = "64b5f3a2e73c9d00123f4567")
        String id,

        @NotBlank(message = "Name is required")
        @Schema(description = "User full name", example = "John Doe", required = true)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "User email address", example = "john@example.com", required = true)
        String email,

        @NotBlank(message = "Role is required")
        @Schema(description = "User role", example = "ADMIN", required = true)
        String role

) {


}
