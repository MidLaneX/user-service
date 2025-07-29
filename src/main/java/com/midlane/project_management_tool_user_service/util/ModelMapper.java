package com.midlane.project_management_tool_user_service.util;

import com.midlane.project_management_tool_user_service.dto.*;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ModelMapper {

    // ========== USER MAPPINGS =========="

    /**
     * Convert User entity to UserResponseDTO
     */
    public UserResponseDTO mapToUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getProfilePictureUrl(),
                user.getJobTitle(),
                user.getDepartment(),
                user.getTeamIds(),
                user.getManagedTeamId(),
                user.getStatus(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * Convert UserRequestDTO to User entity
     */
    public User mapToUser(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .email(dto.email())
                .username(dto.username())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phoneNumber())
                .profilePictureUrl(dto.profilePictureUrl())
                .jobTitle(dto.jobTitle())
                .department(dto.department())
                .teamIds(new ArrayList<>())
                .status(dto.status() != null ? dto.status() : User.UserStatus.ACTIVE)
                .isActive(true)
                .build();
    }

    /**
     * Update existing User entity with data from UserRequestDTO
     */
    public void updateUserFromDTO(User user, UserRequestDTO dto) {
        if (user == null || dto == null) {
            return;
        }

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }
        if (dto.username() != null) {
            user.setUsername(dto.username());
        }
        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
        if (dto.phoneNumber() != null) {
            user.setPhoneNumber(dto.phoneNumber());
        }
        if (dto.profilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.profilePictureUrl());
        }
        if (dto.jobTitle() != null) {
            user.setJobTitle(dto.jobTitle());
        }
        if (dto.department() != null) {
            user.setDepartment(dto.department());
        }
        if (dto.status() != null) {
            user.setStatus(dto.status());
        }
    }

    // ========== TEAM MAPPINGS =========="

    /**
     * Convert Team entity to TeamResponseDTO
     */
    public TeamResponseDTO mapToTeamResponseDTO(Team team) {
        if (team == null) {
            return null;
        }

        return new TeamResponseDTO(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getDepartmentId(),
                team.getTeamLeadId(),
                getTeamLeadName(team.getTeamLeadId()), // Will be resolved by service layer
                team.getType(),
                team.getStatus(),
                team.getMaxMembers(),
                team.getCurrentMemberCount(),
                team.getMemberIds(),
                team.getProjectIds(),
                team.getCreatedAt(),
                team.getUpdatedAt(),
                team.getCreatedBy(),
                team.hasAvailableSlots()
        );
    }

    /**
     * Convert TeamRequestDTO to Team entity
     */
    public Team mapToTeam(TeamRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Team.builder()
                .name(dto.name())
                .description(dto.description())
                .departmentId(dto.departmentId())
                .teamLeadId(dto.teamLeadId())
                .type(dto.type())
                .maxMembers(dto.maxMembers())
                .memberIds(dto.memberIds() != null ? new ArrayList<>(dto.memberIds()) : new ArrayList<>())
                .projectIds(dto.projectIds() != null ? new ArrayList<>(dto.projectIds()) : new ArrayList<>())
                .status(dto.status() != null ? dto.status() : Team.TeamStatus.ACTIVE)
                .build();
    }

    /**
     * Update existing Team entity with data from TeamRequestDTO
     */
    public void updateTeamFromDTO(Team team, TeamRequestDTO dto) {
        if (team == null || dto == null) {
            return;
        }

        if (dto.name() != null) {
            team.setName(dto.name());
        }
        if (dto.description() != null) {
            team.setDescription(dto.description());
        }
        if (dto.departmentId() != null) {
            team.setDepartmentId(dto.departmentId());
        }
        if (dto.teamLeadId() != null) {
            team.setTeamLeadId(dto.teamLeadId());
        }
        if (dto.type() != null) {
            team.setType(dto.type());
        }
        if (dto.maxMembers() > 0) {
            team.setMaxMembers(dto.maxMembers());
        }
        if (dto.status() != null) {
            team.setStatus(dto.status());
        }
        if (dto.memberIds() != null) {
            team.setMemberIds(new ArrayList<>(dto.memberIds()));
        }
        if (dto.projectIds() != null) {
            team.setProjectIds(new ArrayList<>(dto.projectIds()));
        }
    }

    // Helper method to get team lead name (can be enhanced with UserRepository injection)
    private String getTeamLeadName(String teamLeadId) {
        // For now, return null - this can be enhanced to fetch actual name
        // by injecting UserRepository and looking up the user
        return null;
    }
}
