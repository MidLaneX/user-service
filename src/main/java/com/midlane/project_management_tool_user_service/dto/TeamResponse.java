package com.midlane.project_management_tool_user_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private LocalDateTime createdAt;
}
