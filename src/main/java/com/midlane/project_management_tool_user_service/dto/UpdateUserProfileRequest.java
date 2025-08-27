package com.midlane.project_management_tool_user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @JsonProperty("firstName")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @JsonProperty("lastName")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @JsonProperty("jobTitle")
    @Size(max = 100, message = "Job title must be less than 100 characters")
    private String jobTitle;

    @JsonProperty("department")
    @Size(max = 100, message = "Department must be less than 100 characters")
    private String department;
}
