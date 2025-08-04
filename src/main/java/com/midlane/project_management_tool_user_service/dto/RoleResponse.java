package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String permissions;
}
