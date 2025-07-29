package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);

    List<Team> findByType(Team.TeamType type);

    List<Team> findByStatus(Team.TeamStatus status);

    List<Team> findByDepartmentId(String departmentId);

    Optional<Team> findByTeamLeadId(String teamLeadId);

    List<Team> findByCreatedBy(String createdBy);

    @Query("{'memberIds': ?0}")
    List<Team> findTeamsByMemberId(String userId);

    @Query("{'status': 'ACTIVE'}")
    List<Team> findAllActiveTeams();

    @Query("{'status': 'ACTIVE', 'type': ?0}")
    List<Team> findActiveTeamsByType(Team.TeamType type);

    @Query("{'status': 'ACTIVE', 'departmentId': ?0}")
    List<Team> findActiveTeamsByDepartment(String departmentId);

    @Query("{'$expr': {'$lt': [{'$size': {'$ifNull': ['$memberIds', []]}}, '$maxMembers']}}")
    List<Team> findTeamsWithAvailableSlots();
}
