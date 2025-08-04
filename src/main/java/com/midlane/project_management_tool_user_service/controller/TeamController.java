package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.CreateTeamRequest;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable Long orgId,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.createTeam(orgId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeamsByOrganization(@PathVariable Long orgId) {
        List<TeamResponse> teams = teamService.getTeamsByOrganization(orgId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId) {
        TeamResponse team = teamService.getTeamById(teamId);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }
}
