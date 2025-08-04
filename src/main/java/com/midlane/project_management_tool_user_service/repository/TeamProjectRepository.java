package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.TeamProject;
import com.midlane.project_management_tool_user_service.model.TeamProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamProjectRepository extends JpaRepository<TeamProject, TeamProjectId> {

    List<TeamProject> findByProjectId(Long projectId);
    List<TeamProject> findByTeamId(Long teamId);
    boolean existsByTeamIdAndProjectId(Long teamId, Long projectId);
}
