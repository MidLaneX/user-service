package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
    List<Team> findByOrganizationId(Long organizationId);
    List<Team> findByTeamLeadId(String teamLeadId);
    Optional<Team> findByName(String name);
    List<Team> findByType(Team.TeamType type);

    @Query("SELECT t FROM Team t JOIN t.memberIds m WHERE m = :memberId")
    List<Team> findTeamsByMemberId(@Param("memberId") String memberId);

    @Query("SELECT t FROM Team t WHERE SIZE(t.memberIds) < t.maxMembers")
    List<Team> findTeamsWithAvailableSlots();
}
