package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;

@Data
public class MeResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
}
