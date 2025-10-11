package com.midlane.project_management_tool_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailsResponse {
    private Long memberId;
    private String name;
    private String email;
    private String role;
    private boolean isTeamLead;
}
