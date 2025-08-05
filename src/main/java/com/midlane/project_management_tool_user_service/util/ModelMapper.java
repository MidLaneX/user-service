package com.midlane.project_management_tool_user_service.util;

import com.midlane.project_management_tool_user_service.dto.TeamRequestDTO;
import com.midlane.project_management_tool_user_service.dto.TeamResponseDTO;
import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ModelMapper {

    public Team mapToTeam(TeamRequestDTO teamRequestDTO) {
        Team team = new Team();
        team.setName(teamRequestDTO.name());
        team.setDescription(teamRequestDTO.description());
        team.setTeamLeadId(teamRequestDTO.teamLeadId());
        team.setManagedTeamId(teamRequestDTO.teamLeadId());
        team.setMaxMembers(teamRequestDTO.maxMembers());
        team.setMemberIds(new ArrayList<>());

        if (teamRequestDTO.type() != null && !teamRequestDTO.type().isBlank()) {
            try {
                team.setType(Team.TeamType.valueOf(teamRequestDTO.type().toUpperCase()));
            } catch (IllegalArgumentException e) {
                team.setType(Team.TeamType.DEVELOPMENT);
            }
        }

        return team;
    }

    public TeamResponseDTO mapToTeamResponseDTO(Team team) {
        return new TeamResponseDTO(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getTeamLeadId(),
            team.getManagedTeamId(),
            team.getType() != null ? team.getType().name() : null,
            team.getStatus() != null ? team.getStatus().name() : null,
            team.getMaxMembers(),
            team.getMemberIds(),
            team.getCreatedBy(),
            team.getCreatedAt(),
            team.getUpdatedAt()
        );
    }

    public void updateTeamFromDTO(Team existingTeam, TeamRequestDTO teamRequestDTO) {
        existingTeam.setName(teamRequestDTO.name());
        existingTeam.setDescription(teamRequestDTO.description());
        existingTeam.setTeamLeadId(teamRequestDTO.teamLeadId());
        existingTeam.setMaxMembers(teamRequestDTO.maxMembers());

        if (teamRequestDTO.type() != null && !teamRequestDTO.type().isBlank()) {
            try {
                existingTeam.setType(Team.TeamType.valueOf(teamRequestDTO.type().toUpperCase()));
            } catch (IllegalArgumentException e) {
                existingTeam.setType(Team.TeamType.DEVELOPMENT);
            }
        }
    }

    public User mapToUser(UserRequestDTO userRequestDTO) {
        User user = new User();
        user.setUsername(userRequestDTO.username());
        return user;
    }

    public UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getStatus() != null ? user.getStatus().name() : null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}