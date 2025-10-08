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
public class MemberAddedToTeamEvent {
    private Long teamId;
    private Long memberId;
    private String role;
    private LocalDateTime timestamp;
    private String eventType;
}
