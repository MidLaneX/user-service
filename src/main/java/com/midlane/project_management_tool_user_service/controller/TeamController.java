package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
import com.midlane.project_management_tool_user_service.dto.MemberDetailsResponse;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @RequestParam Long creatorId) {
        System.out.println("Creating team with request: " + request + " by user ID: " + creatorId);
        TeamResponse response = teamService.createTeam(request, creatorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody CreateTeamRequest request,
            @RequestParam Long requesterId) {
        TeamResponse response = teamService.updateTeam(id, request, requesterId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
        teamService.deleteTeam(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {
        teamService.addMember(teamId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<MemberDetailsResponse>> getTeamMembers(@PathVariable Long teamId) {
        List<MemberDetailsResponse> members = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/organization/{organizationId}/user/{userId}")
    public ResponseEntity<List<TeamResponse>> getTeamsByOrganizationAndUser(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {
        log.info("Getting teams for organization {} where user {} is a member", organizationId, userId);
        List<TeamResponse> teams = teamService.getTeamsByOrganizationAndMember(organizationId, userId);
        return ResponseEntity.ok(teams);
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {
        teamService.removeMember(teamId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{teamId}/lead/{userId}")
    public ResponseEntity<Void> setTeamLead(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {
        teamService.setTeamLead(teamId, userId, requesterId);
        return ResponseEntity.ok().build();
    }
}
