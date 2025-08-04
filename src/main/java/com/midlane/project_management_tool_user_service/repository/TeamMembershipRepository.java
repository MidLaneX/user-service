package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.TeamMembership;
import com.midlane.project_management_tool_user_service.model.TeamMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, TeamMembershipId> {

    List<TeamMembership> findByTeamId(Long teamId);
    List<TeamMembership> findByUserId(Long userId);
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
