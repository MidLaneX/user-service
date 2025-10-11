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
public class TeamCreatedEvent {
    private Long teamId;
    private String teamName;
    private String description;
    private Long ownerId;
    private LocalDateTime timestamp;
    private String eventType;
}
