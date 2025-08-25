package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private String teamType;
    private String status;
    private Integer maxMembers;
    private Integer currentMemberCount;
    private Long organizationId;
    private String organizationName;
    private Long teamLeadId;
    private String teamLeadName;
    private String teamLeadEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasAvailableSlots;
}
