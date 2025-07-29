package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.TeamRequestDTO;
import com.midlane.project_management_tool_user_service.dto.TeamResponseDTO;
import com.midlane.project_management_tool_user_service.model.Team;
import com.midlane.project_management_tool_user_service.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Team Management", description = "Team CRUD operations and member management")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Create a new team", description = "Create a new team with basic information")
    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(
            @Valid @RequestBody TeamRequestDTO teamRequestDTO,
            @RequestHeader(value = "X-User-ID", required = false, defaultValue = "system") String createdBy) {
        log.info("Creating team with name: {}", teamRequestDTO.name());
        TeamResponseDTO createdTeam = teamService.createTeam(teamRequestDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @Operation(summary = "Get all teams", description = "Retrieve all teams in the system")
    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        log.info("Fetching all teams");
        List<TeamResponseDTO> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get teams with pagination", description = "Retrieve teams with pagination support")
    @GetMapping("/paginated")
    public ResponseEntity<Page<TeamResponseDTO>> getAllTeams(Pageable pageable) {
        log.info("Fetching teams with pagination");
        Page<TeamResponseDTO> teams = teamService.getAllTeams(pageable);
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get team by ID", description = "Retrieve a specific team by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamById(
            @Parameter(description = "Team ID") @PathVariable String id) {
        log.info("Fetching team with ID: {}", id);
        TeamResponseDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Get team by name", description = "Retrieve a team by their name")
    @GetMapping("/name/{name}")
    public ResponseEntity<TeamResponseDTO> getTeamByName(
            @Parameter(description = "Team name") @PathVariable String name) {
        log.info("Fetching team with name: {}", name);
        TeamResponseDTO team = teamService.getTeamByName(name);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Update team", description = "Update an existing team's information")
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @Parameter(description = "Team ID") @PathVariable String id,
            @Valid @RequestBody TeamRequestDTO teamRequestDTO) {
        log.info("Updating team with ID: {}", id);
        TeamResponseDTO updatedTeam = teamService.updateTeam(id, teamRequestDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    @Operation(summary = "Delete team", description = "Delete a team from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID") @PathVariable String id) {
        log.info("Deleting team with ID: {}", id);
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add member to team", description = "Add a user as a member to the team")
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamResponseDTO> addMemberToTeam(
            @Parameter(description = "Team ID") @PathVariable String teamId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Adding user {} to team {}", userId, teamId);
        TeamResponseDTO team = teamService.addMemberToTeam(teamId, userId);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Remove member from team", description = "Remove a user from the team")
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamResponseDTO> removeMemberFromTeam(
            @Parameter(description = "Team ID") @PathVariable String teamId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Removing user {} from team {}", userId, teamId);
        TeamResponseDTO team = teamService.removeMemberFromTeam(teamId, userId);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Get teams by department", description = "Retrieve teams by department ID")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsByDepartment(
            @Parameter(description = "Department ID") @PathVariable String departmentId) {
        log.info("Fetching teams for department: {}", departmentId);
        List<TeamResponseDTO> teams = teamService.getTeamsByDepartment(departmentId);
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get teams by type", description = "Retrieve teams by their type")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsByType(
            @Parameter(description = "Team type") @PathVariable Team.TeamType type) {
        log.info("Fetching teams of type: {}", type);
        List<TeamResponseDTO> teams = teamService.getTeamsByType(type);
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get teams for user", description = "Retrieve all teams where user is a member")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsForUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Fetching teams for user: {}", userId);
        List<TeamResponseDTO> teams = teamService.getTeamsForUser(userId);
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get team managed by user", description = "Retrieve team managed by user (if user is team lead)")
    @GetMapping("/managed-by/{userId}")
    public ResponseEntity<TeamResponseDTO> getTeamManagedByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Fetching team managed by user: {}", userId);
        TeamResponseDTO team = teamService.getTeamManagedByUser(userId);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Get teams with available slots", description = "Retrieve teams that have available member slots")
    @GetMapping("/available-slots")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsWithAvailableSlots() {
        log.info("Fetching teams with available slots");
        List<TeamResponseDTO> teams = teamService.getTeamsWithAvailableSlots();
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Toggle team status", description = "Activate or deactivate a team")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<TeamResponseDTO> toggleTeamStatus(
            @Parameter(description = "Team ID") @PathVariable String id) {
        log.info("Toggling status for team with ID: {}", id);
        TeamResponseDTO team = teamService.toggleTeamStatus(id);
        return ResponseEntity.ok(team);
    }
}
