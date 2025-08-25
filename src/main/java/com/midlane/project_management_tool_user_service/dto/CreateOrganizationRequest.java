package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 200, message = "Website URL cannot exceed 200 characters")
    private String website;

    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    @Size(max = 50, message = "Size cannot exceed 50 characters")
    private String size;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;
}
