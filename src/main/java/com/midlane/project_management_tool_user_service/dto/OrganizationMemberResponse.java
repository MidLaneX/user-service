package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMemberResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String jobTitle;
    private String department;
}