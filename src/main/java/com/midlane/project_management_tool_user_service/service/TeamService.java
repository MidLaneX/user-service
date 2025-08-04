package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;

    public TeamResponse createTeam(Long organizationId, CreateTeamRequest request) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new RuntimeException("Organization not found");
        }

        if (teamRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new RuntimeException("Team with this name already exists in this organization");
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setOrganizationId(organizationId);

        Team savedTeam = teamRepository.save(team);
        return mapToTeamResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByOrganization(Long organizationId) {
        return teamRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return mapToTeamResponse(team);
    }

    public TeamResponse updateTeam(Long id, CreateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getName().equals(request.getName()) &&
            teamRepository.existsByNameAndOrganizationId(request.getName(), team.getOrganizationId())) {
            throw new RuntimeException("Team with this name already exists in this organization");
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        Team savedTeam = teamRepository.save(team);
        return mapToTeamResponse(savedTeam);
    }

    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Team not found");
        }
        teamRepository.deleteById(id);
    }

    private TeamResponse mapToTeamResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setDescription(team.getDescription());
        response.setOrganizationId(team.getOrganizationId());
        response.setCreatedAt(team.getCreatedAt());
        return response;
    }
}
