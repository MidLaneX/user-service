package com.midlane.project_management_tool_user_service.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMembershipId implements Serializable {

    private Long userId;
    private Long teamId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMembershipId that = (TeamMembershipId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, teamId);
    }
}
