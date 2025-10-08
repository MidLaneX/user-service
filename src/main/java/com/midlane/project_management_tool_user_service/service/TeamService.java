package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
import com.midlane.project_management_tool_user_service.dto.MemberDetailsResponse;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TeamEventProducerService teamEventProducerService;

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, Long creatorId) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + request.getOrganizationId()));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + creatorId));

        // Check if creator is member of organization
        if (!organization.getMembers().contains(creator)) {
            throw new RuntimeException("User must be a member of the organization to create teams");
        }

        Team.TeamType teamType = Team.TeamType.DEVELOPMENT;
        if (request.getTeamType() != null) {
            try {
                teamType = Team.TeamType.valueOf(request.getTeamType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid team type: " + request.getTeamType());
            }
        }

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teamType(teamType)
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 10)
                .organization(organization)
                .status(Team.TeamStatus.ACTIVE)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Auto-add creator as team member
        savedTeam.addMember(creator);
        savedTeam = teamRepository.save(savedTeam);

        log.info("Team created: id={}, name={}, orgId={}, creator={}",
                savedTeam.getId(), savedTeam.getName(), request.getOrganizationId(), creator.getEmail());

        // Publish team creation event
        try {
            teamEventProducerService.publishTeamCreatedEvent(
                    savedTeam.getId(),
                    savedTeam.getName(),
                    savedTeam.getDescription(),
                    creatorId
            );
            log.info("Successfully published team created event for teamId: {}", savedTeam.getId());
        } catch (Exception e) {
            log.error("Failed to publish team created event for teamId: {}", savedTeam.getId(), e);
            // Don't fail the team creation if event publishing fails
        }

        // Publish member added to team event for the creator (as owner/admin role)
        try {
            teamEventProducerService.publishMemberAddedToTeamEvent(
                    savedTeam.getId(),
                    creatorId,
                    "OWNER" // Creator gets owner role
            );
            log.info("Successfully published member added to team event for creator: {}", creatorId);
        } catch (Exception e) {
            log.error("Failed to publish member added to team event for creator: {}", creatorId, e);
            // Don't fail the team creation if event publishing fails
        }

        return mapToResponse(savedTeam);
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));
        return mapToResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, CreateTeamRequest request, Long requesterId) {
        Team existingTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester has permission
        if (!canManageTeam(existingTeam, requester)) {
            throw new RuntimeException("Insufficient permissions to update team");
        }

        // Update fields
        existingTeam.setName(request.getName());
        existingTeam.setDescription(request.getDescription());

        if (request.getTeamType() != null) {
            try {
                existingTeam.setTeamType(Team.TeamType.valueOf(request.getTeamType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid team type: " + request.getTeamType());
            }
        }

        if (request.getMaxMembers() != null) {
            existingTeam.setMaxMembers(request.getMaxMembers());
        }

        Team savedTeam = teamRepository.save(existingTeam);
        return mapToResponse(savedTeam);
    }

    @Transactional
    public void deleteTeam(Long teamId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester is organization owner
        if (!team.getOrganization().getOwner().equals(requester)) {
            throw new RuntimeException("Only organization owner can delete teams");
        }

        teamRepository.delete(team);

        log.info("Team deleted: id={}, name={}", teamId, team.getName());
    }

    @Transactional
    public void addMember(Long teamId, Long userId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester has permission (owner, team lead, or admin)
        if (!canManageTeam(team, requester)) {
            throw new RuntimeException("Insufficient permissions to add team members");
        }

        // Check if user is member of the organization
        if (!team.getOrganization().getMembers().contains(user)) {
            throw new RuntimeException("User must be a member of the organization to join teams");
        }

        // Check if user is already a member
        if (team.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member of this team");
        }

        team.addMember(user);
        Team savedTeam = teamRepository.save(team);

        log.info("User added to team: userId={}, teamId={}, teamName={}",
                userId, teamId, team.getName());

        // Enhanced logging for Kafka event publishing
        log.info("Attempting to publish team member added event for userId: {}, teamId: {}", userId, teamId);
        // Publish Kafka event after successful team member addition (existing event)
        try {
            teamEventProducerService.publishTeamMemberAddedEvent(
                userId,
                team.getOrganization().getId(),
                teamId,
                team.getName(),
                team.getOrganization().getName(),
                user.getEmail(),
                user.getFullName()
            );
            log.info("Successfully published team member added event for userId: {}, teamId: {}", userId, teamId);
        } catch (Exception e) {
            log.error("Failed to publish team member added event for userId: {}, teamId: {}", userId, teamId, e);
            // Note: We don't re-throw here to avoid rolling back the transaction
            // The team member addition should still succeed even if event publishing fails
        }

        // Publish new member added to team event (for collaboration service)
        try {
            teamEventProducerService.publishMemberAddedToTeamEvent(
                    teamId,
                    userId,
                    "MEMBER" // Default role for added members
            );
            log.info("Successfully published member added to team event for userId: {}, teamId: {}", userId, teamId);
        } catch (Exception e) {
            log.error("Failed to publish member added to team event for userId: {}, teamId: {}", userId, teamId, e);
        }
    }

    @Transactional
    public void removeMember(Long teamId, Long userId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester has permission (owner, team lead, or admin, or user removing themselves)
        if (!canManageTeam(team, requester) && !userId.equals(requesterId)) {
            throw new RuntimeException("Insufficient permissions to remove team members");
        }

        team.removeMember(user);
        teamRepository.save(team);

        log.info("User removed from team: userId={}, teamId={}, teamName={}",
                userId, teamId, team.getName());

        // Publish team member removed event
        try {
            teamEventProducerService.publishTeamMemberRemovedEvent(teamId, userId);
            log.info("Successfully published team member removed event for userId: {}, teamId: {}", userId, teamId);
        } catch (Exception e) {
            log.error("Failed to publish team member removed event for userId: {}, teamId: {}", userId, teamId, e);
            // Don't fail the member removal if event publishing fails
        }
    }

    @Transactional
    public void setTeamLead(Long teamId, Long userId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester is organization owner or current team lead
        if (!team.getOrganization().getOwner().equals(requester) &&
            !team.isTeamLead(requester)) {
            throw new RuntimeException("Only organization owner or current team lead can assign team leadership");
        }

        // Check if user is team member
        if (!team.getMembers().contains(user)) {
            throw new RuntimeException("User must be a team member to become team lead");
        }

        team.setTeamLead(user);
        teamRepository.save(team);

        log.info("Team lead assigned: userId={}, teamId={}, teamName={}",
                userId, teamId, team.getName());
    }

    public List<Team> getTeamsByOrganization(Long organizationId) {
        return teamRepository.findByOrganizationId(organizationId);
    }

    public List<Team> getTeamsByMember(Long userId) {
        return teamRepository.findByMemberId(userId);
    }

    public List<Team> getTeamsByLead(Long userId) {
        return teamRepository.findByTeamLeadId(userId);
    }

    public List<TeamResponse> getTeamsByOrganizationAndMember(Long organizationId, Long userId) {
        // Verify that the organization exists
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        // Verify that the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Get all teams where the user is a member
        List<Team> userTeams = teamRepository.findByMemberId(userId);

        // Filter teams by organization ID and map to response
        return userTeams.stream()
                .filter(team -> team.getOrganization().getId().equals(organizationId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<Team> getTeamsWithAvailableSlots() {
        return teamRepository.findTeamsWithAvailableSlots();
    }

    public boolean isUserMember(Long teamId, Long userId) {
        return teamRepository.isUserMemberOfTeam(teamId, userId);
    }

    public boolean isUserTeamLead(Long teamId, Long userId) {
        return teamRepository.isUserTeamLead(teamId, userId);
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .teamType(team.getTeamType().toString())
                .status(team.getStatus().toString())
                .maxMembers(team.getMaxMembers())
                .currentMemberCount(team.getCurrentMemberCount())
                .organizationId(team.getOrganization().getId())
                .organizationName(team.getOrganization().getName())
                .teamLeadId(team.getTeamLead() != null ? team.getTeamLead().getId() : null)
                .teamLeadName(team.getTeamLead() != null ? team.getTeamLead().getFullName() : null)
                .teamLeadEmail(team.getTeamLead() != null ? team.getTeamLead().getEmail() : null)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .hasAvailableSlots(team.hasAvailableSlots())
                .build();
    }

    private boolean canManageTeam(Team team, User user) {
        // Organization owner can manage all teams
        if (team.getOrganization().getOwner().equals(user)) {
            return true;
        }

        // Team lead can manage their team
        if (team.isTeamLead(user)) {
            return true;
        }

        return false;
    }

    public List<MemberDetailsResponse> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));

        return team.getMembers().stream()
                .map(member -> MemberDetailsResponse.builder()
                        .memberId(member.getUserId())
                        .name(member.getFullName())
                        .email(member.getEmail())
                        .role(member.getRole() != null ? member.getRole().getName() : "USER")
                        .isTeamLead(team.getTeamLead() != null && team.getTeamLead().equals(member))
                        .build())
                .collect(Collectors.toList());
    }
}
