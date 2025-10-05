package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private Long ownerId;
    private String ownerEmail;
    private String ownerName;
    private Integer memberCount;
    private Integer teamCount;
    private Boolean isOwner;
    private Boolean isMember;
}
