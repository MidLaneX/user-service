package com.midlane.project_management_tool_user_service.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamProjectId implements Serializable {

    private Long teamId;
    private Long projectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamProjectId that = (TeamProjectId) o;
        return Objects.equals(teamId, that.teamId) &&
               Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, projectId);
    }
}
