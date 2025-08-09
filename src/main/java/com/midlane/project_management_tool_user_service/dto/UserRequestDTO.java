package com.midlane.project_management_tool_user_service.dto;

    import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "First Name is required")
        @Size(min = 2, max = 50, message = "First Name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last Name is required")
        @Size(min = 2, max = 50, message = "Last Name must be between 2 and 50 characters")
        String lastName
) {}
