package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.AssignTeamToProjectRequest;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.model.TeamProject;
import com.midlane.project_management_tool_user_service.model.TeamProjectId;
import com.midlane.project_management_tool_user_service.repository.TeamProjectRepository;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamProjectService {

    private final TeamProjectRepository teamProjectRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    public void assignTeamToProject(Long projectId, AssignTeamToProjectRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found");
        }
        if (!teamRepository.existsById(request.getTeamId())) {
            throw new RuntimeException("Team not found");
        }
        if (teamProjectRepository.existsByTeamIdAndProjectId(request.getTeamId(), projectId)) {
            throw new RuntimeException("Team is already assigned to this project");
        }

        TeamProject teamProject = new TeamProject();
        teamProject.setTeamId(request.getTeamId());
        teamProject.setProjectId(projectId);

        teamProjectRepository.save(teamProject);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getProjectTeams(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found");
        }

        List<TeamProject> assignments = teamProjectRepository.findByProjectId(projectId);
        List<Long> teamIds = assignments.stream()
                .map(TeamProject::getTeamId)
                .collect(Collectors.toList());

        return teamRepository.findAllById(teamIds).stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }

    public void removeTeamFromProject(Long projectId, Long teamId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found");
        }
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }

        TeamProjectId assignmentId = new TeamProjectId(teamId, projectId);
        if (!teamProjectRepository.existsById(assignmentId)) {
            throw new RuntimeException("Team is not assigned to this project");
        }

        teamProjectRepository.deleteById(assignmentId);
    }

    private TeamResponse mapToTeamResponse(com.midlane.project_management_tool_user_service.model.Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setDescription(team.getDescription());
        response.setOrganizationId(team.getOrganizationId());
        response.setCreatedAt(team.getCreatedAt());
        return response;
    }
}
