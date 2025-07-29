package com.midlane.project_management_tool_user_service.dto;

import com.midlane.project_management_tool_user_service.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        String phoneNumber,

        String profilePictureUrl,

        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        List<String> teamIds,

        String managedTeamId,

        User.UserStatus status
) {
}
