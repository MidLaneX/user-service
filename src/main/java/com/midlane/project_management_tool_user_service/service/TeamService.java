package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.TeamRequestDTO;
import com.midlane.project_management_tool_user_service.dto.TeamResponseDTO;
import com.midlane.project_management_tool_user_service.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {

    /**
     * Create a new team
     */
    TeamResponseDTO createTeam(TeamRequestDTO teamRequestDTO, String createdBy);

    /**
     * Get all teams
     */
    List<TeamResponseDTO> getAllTeams();

    /**
     * Get teams with pagination
     */
    Page<TeamResponseDTO> getAllTeams(Pageable pageable);

    /**
     * Get team by ID
     */
    TeamResponseDTO getTeamById(String id);

    /**
     * Get team by name
     */
    TeamResponseDTO getTeamByName(String name);

    /**
     * Update team
     */
    TeamResponseDTO updateTeam(String id, TeamRequestDTO teamRequestDTO);

    /**
     * Delete team
     */
    void deleteTeam(String id);

    /**
     * Add member to team
     */
    TeamResponseDTO addMemberToTeam(String teamId, String userId);

    /**
     * Remove member from team
     */
    TeamResponseDTO removeMemberFromTeam(String teamId, String userId);

    /**
     * Get teams by department
     */
    List<TeamResponseDTO> getTeamsByDepartment(String departmentId);

    /**
     * Get teams by type
     */
    List<TeamResponseDTO> getTeamsByType(Team.TeamType type);

    /**
     * Get teams for a user
     */
    List<TeamResponseDTO> getTeamsForUser(String userId);

    /**
     * Get team managed by user (if user is team lead)
     */
    TeamResponseDTO getTeamManagedByUser(String userId);

    /**
     * Get teams with available slots
     */
    List<TeamResponseDTO> getTeamsWithAvailableSlots();

    /**
     * Toggle team status
     */
    TeamResponseDTO toggleTeamStatus(String id);
}
