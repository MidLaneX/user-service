package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberAddedEvent {
    private Long userId;
    private Long organizationId;
    private Long teamId;
    private String role;
    private LocalDateTime timestamp;
    private String eventType;

    // Additional context fields
    private String teamName;
    private String organizationName;
    private String userEmail;
    private String userName;
}
