package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.AddTeamMemberRequest;
import com.midlane.project_management_tool_user_service.dto.UserResponse;
import com.midlane.project_management_tool_user_service.model.TeamMembership;
import com.midlane.project_management_tool_user_service.model.TeamMembershipId;
import com.midlane.project_management_tool_user_service.repository.TeamMembershipRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import com.midlane.project_management_tool_user_service.repository.TeamRepository;
import com.midlane.project_management_tool_user_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamMembershipService {

    private final TeamMembershipRepository teamMembershipRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;

    public void addTeamMember(Long teamId, AddTeamMemberRequest request) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new RuntimeException("User not found");
        }
        if (!roleRepository.existsById(request.getRoleId())) {
            throw new RuntimeException("Role not found");
        }
        if (teamMembershipRepository.existsByUserIdAndTeamId(request.getUserId(), teamId)) {
            throw new RuntimeException("User is already a member of this team");
        }

        TeamMembership membership = new TeamMembership();
        membership.setUserId(request.getUserId());
        membership.setTeamId(teamId);
        membership.setRoleId(request.getRoleId());

        teamMembershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getTeamMembers(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }

        List<TeamMembership> memberships = teamMembershipRepository.findByTeamId(teamId);
        List<Long> userIds = memberships.stream()
                .map(TeamMembership::getUserId)
                .collect(Collectors.toList());

        return userRepository.findAllById(userIds).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void removeTeamMember(Long teamId, Long userId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        TeamMembershipId membershipId = new TeamMembershipId(userId, teamId);
        if (!teamMembershipRepository.existsById(membershipId)) {
            throw new RuntimeException("User is not a member of this team");
        }

        teamMembershipRepository.deleteById(membershipId);
    }

    private UserResponse mapToUserResponse(com.midlane.project_management_tool_user_service.model.User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
