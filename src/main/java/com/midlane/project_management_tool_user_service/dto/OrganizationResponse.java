package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrganizationResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;
}
