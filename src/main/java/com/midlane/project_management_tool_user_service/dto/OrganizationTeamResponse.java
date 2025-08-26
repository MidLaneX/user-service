package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationTeamResponse {
    private Long teamId;
    private String teamName;
    private String teamType;
    private String description;
    private Integer memberCount;
}
