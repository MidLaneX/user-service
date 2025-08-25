package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // Find teams by organization
    List<Team> findByOrganization(Organization organization);

    // Find teams by organization ID
    List<Team> findByOrganizationId(Long organizationId);

    // Find teams by team lead
    List<Team> findByTeamLead(User teamLead);

    // Find teams by team lead ID
    List<Team> findByTeamLeadId(Long teamLeadId);

    // Find teams by name (case-insensitive)
    List<Team> findByNameContainingIgnoreCase(String name);

    // Find teams by status
    List<Team> findByStatus(Team.TeamStatus status);

    // Find teams by type
    List<Team> findByTeamType(Team.TeamType teamType);

    // Find teams where user is a member
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findByMemberId(@Param("userId") Long userId);

    // Check if user is member of team
    @Query("SELECT COUNT(t) > 0 FROM Team t JOIN t.members m WHERE t.id = :teamId AND m.id = :userId")
    boolean isUserMemberOfTeam(@Param("teamId") Long teamId, @Param("userId") Long userId);

    // Check if user is team lead
    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE t.id = :teamId AND t.teamLead.id = :userId")
    boolean isUserTeamLead(@Param("teamId") Long teamId, @Param("userId") Long userId);

    // Find teams with available slots
    @Query("SELECT t FROM Team t WHERE SIZE(t.members) < t.maxMembers AND t.status = 'ACTIVE'")
    List<Team> findTeamsWithAvailableSlots();

    // Find teams by organization and status
    List<Team> findByOrganizationAndStatus(Organization organization, Team.TeamStatus status);

    // Count teams by organization
    long countByOrganization(Organization organization);

    // Count teams led by user
    long countByTeamLead(User teamLead);
}
