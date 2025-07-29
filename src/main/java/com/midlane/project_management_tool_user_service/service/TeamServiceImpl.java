package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.TeamRequestDTO;
import com.midlane.project_management_tool_user_service.dto.TeamResponseDTO;
import com.midlane.project_management_tool_user_service.exception.DuplicateResourceException;
import com.midlane.project_management_tool_user_service.exception.UserNotFoundException;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import com.midlane.project_management_tool_user_service.util.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public TeamResponseDTO createTeam(TeamRequestDTO teamRequestDTO, String createdBy) {
        log.info("Creating team with name: {}", teamRequestDTO.name());

        // Check for duplicate team name
        if (teamRepository.existsByName(teamRequestDTO.name())) {
            throw new DuplicateResourceException("Team with name already exists: " + teamRequestDTO.name());
        }

        // Validate team lead exists if provided
        if (teamRequestDTO.teamLeadId() != null) {
            userRepository.findById(teamRequestDTO.teamLeadId())
                    .orElseThrow(() -> new UserNotFoundException("Team lead not found with ID: " + teamRequestDTO.teamLeadId()));
        }

        Team team = modelMapper.mapToTeam(teamRequestDTO);
        team.setCreatedBy(createdBy);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());

        if (team.getStatus() == null) {
            team.setStatus(Team.TeamStatus.ACTIVE);
        }

        if (team.getMemberIds() == null) {
            team.setMemberIds(new ArrayList<>());
        }

        // Add team lead as member if specified
        if (teamRequestDTO.teamLeadId() != null && !team.getMemberIds().contains(teamRequestDTO.teamLeadId())) {
            team.getMemberIds().add(teamRequestDTO.teamLeadId());
        }

        Team savedTeam = teamRepository.save(team);

        // Update team lead's managed team ID
        if (teamRequestDTO.teamLeadId() != null) {
            updateUserManagedTeam(teamRequestDTO.teamLeadId(), savedTeam.getId());
        }

        log.info("Team created successfully with ID: {}", savedTeam.getId());
        return modelMapper.mapToTeamResponseDTO(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeams() {
        log.info("Fetching all teams");
        return teamRepository.findAll()
                .stream()
                .map(modelMapper::mapToTeamResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponseDTO> getAllTeams(Pageable pageable) {
        log.info("Fetching teams with pagination: {}", pageable);
        return teamRepository.findAll(pageable)
                .map(modelMapper::mapToTeamResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(String id) {
        log.info("Fetching team with ID: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + id));
        return modelMapper.mapToTeamResponseDTO(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamByName(String name) {
        log.info("Fetching team with name: {}", name);
        Team team = teamRepository.findByName(name)
                .orElseThrow(() -> new UserNotFoundException("Team not found with name: " + name));
        return modelMapper.mapToTeamResponseDTO(team);
    }

    @Override
    public TeamResponseDTO updateTeam(String id, TeamRequestDTO teamRequestDTO) {
        log.info("Updating team with ID: {}", id);

        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + id));

        // Check for duplicate name if name is being changed
        if (!existingTeam.getName().equals(teamRequestDTO.name()) &&
            teamRepository.existsByName(teamRequestDTO.name())) {
            throw new DuplicateResourceException("Team with name already exists: " + teamRequestDTO.name());
        }

        String oldTeamLeadId = existingTeam.getTeamLeadId();

        modelMapper.updateTeamFromDTO(existingTeam, teamRequestDTO);
        existingTeam.setUpdatedAt(LocalDateTime.now());

        // Handle team lead changes
        if (!java.util.Objects.equals(oldTeamLeadId, teamRequestDTO.teamLeadId())) {
            // Remove old team lead's managed team reference
            if (oldTeamLeadId != null) {
                updateUserManagedTeam(oldTeamLeadId, null);
            }

            // Set new team lead's managed team reference
            if (teamRequestDTO.teamLeadId() != null) {
                updateUserManagedTeam(teamRequestDTO.teamLeadId(), existingTeam.getId());

                // Add new team lead as member if not already
                if (!existingTeam.getMemberIds().contains(teamRequestDTO.teamLeadId())) {
                    existingTeam.getMemberIds().add(teamRequestDTO.teamLeadId());
                }
            }
        }

        Team updatedTeam = teamRepository.save(existingTeam);
        log.info("Team updated successfully with ID: {}", updatedTeam.getId());

        return modelMapper.mapToTeamResponseDTO(updatedTeam);
    }

    @Override
    public void deleteTeam(String id) {
        log.info("Deleting team with ID: {}", id);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + id));

        // Remove team references from all members
        for (String memberId : team.getMemberIds()) {
            removeTeamFromUser(memberId, id);
        }

        // Remove managed team reference from team lead
        if (team.getTeamLeadId() != null) {
            updateUserManagedTeam(team.getTeamLeadId(), null);
        }

        teamRepository.deleteById(id);
        log.info("Team deleted successfully with ID: {}", id);
    }

    @Override
    public TeamResponseDTO addMemberToTeam(String teamId, String userId) {
        log.info("Adding user {} to team {}", userId, teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (team.getMemberIds().contains(userId)) {
            throw new DuplicateResourceException("User is already a member of this team");
        }

        if (!team.hasAvailableSlots()) {
            throw new DuplicateResourceException("Team has reached maximum capacity");
        }

        team.getMemberIds().add(userId);
        team.setUpdatedAt(LocalDateTime.now());

        // Add team to user's team list
        if (user.getTeamIds() == null) {
            user.setTeamIds(new ArrayList<>());
        }
        user.getTeamIds().add(teamId);
        user.setUpdatedAt(LocalDateTime.now());

        teamRepository.save(team);
        userRepository.save(user);

        log.info("User {} added to team {} successfully", userId, teamId);
        return modelMapper.mapToTeamResponseDTO(team);
    }

    @Override
    public TeamResponseDTO removeMemberFromTeam(String teamId, String userId) {
        log.info("Removing user {} from team {}", userId, teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + teamId));

        if (!team.getMemberIds().contains(userId)) {
            throw new UserNotFoundException("User is not a member of this team");
        }

        team.getMemberIds().remove(userId);
        team.setUpdatedAt(LocalDateTime.now());

        // Remove team from user's team list
        removeTeamFromUser(userId, teamId);

        teamRepository.save(team);

        log.info("User {} removed from team {} successfully", userId, teamId);
        return modelMapper.mapToTeamResponseDTO(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsByDepartment(String departmentId) {
        log.info("Fetching teams for department: {}", departmentId);
        return teamRepository.findByDepartmentId(departmentId)
                .stream()
                .map(modelMapper::mapToTeamResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsByType(Team.TeamType type) {
        log.info("Fetching teams of type: {}", type);
        return teamRepository.findByType(type)
                .stream()
                .map(modelMapper::mapToTeamResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsForUser(String userId) {
        log.info("Fetching teams for user: {}", userId);
        return teamRepository.findTeamsByMemberId(userId)
                .stream()
                .map(modelMapper::mapToTeamResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamManagedByUser(String userId) {
        log.info("Fetching team managed by user: {}", userId);
        Team team = teamRepository.findByTeamLeadId(userId)
                .orElseThrow(() -> new UserNotFoundException("No team found managed by user: " + userId));
        return modelMapper.mapToTeamResponseDTO(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsWithAvailableSlots() {
        log.info("Fetching teams with available slots");
        return teamRepository.findTeamsWithAvailableSlots()
                .stream()
                .map(modelMapper::mapToTeamResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponseDTO toggleTeamStatus(String id) {
        log.info("Toggling status for team with ID: {}", id);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Team not found with ID: " + id));

        team.setStatus(team.getStatus() == Team.TeamStatus.ACTIVE ?
                      Team.TeamStatus.INACTIVE : Team.TeamStatus.ACTIVE);
        team.setUpdatedAt(LocalDateTime.now());

        Team updatedTeam = teamRepository.save(team);
        log.info("Team status toggled successfully. ID: {}, Status: {}", id, updatedTeam.getStatus());

        return modelMapper.mapToTeamResponseDTO(updatedTeam);
    }

    // Helper methods
    private void updateUserManagedTeam(String userId, String teamId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setManagedTeamId(teamId);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private void removeTeamFromUser(String userId, String teamId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getTeamIds() != null) {
                user.getTeamIds().remove(teamId);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        });
    }
}
