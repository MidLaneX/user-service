package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.AssignTeamToProjectRequest;
import com.midlane.project_management_tool_user_service.dto.TeamResponse;
import com.midlane.project_management_tool_user_service.service.TeamProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/teams")
@RequiredArgsConstructor
public class TeamProjectController {

    private final TeamProjectService teamProjectService;

    @PostMapping
    public ResponseEntity<Void> assignTeamToProject(
            @PathVariable Long projectId,
            @Valid @RequestBody AssignTeamToProjectRequest request) {
        teamProjectService.assignTeamToProject(projectId, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getProjectTeams(@PathVariable Long projectId) {
        List<TeamResponse> teams = teamProjectService.getProjectTeams(projectId);
        return ResponseEntity.ok(teams);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> removeTeamFromProject(
            @PathVariable Long projectId,
            @PathVariable Long teamId) {
        teamProjectService.removeTeamFromProject(projectId, teamId);
        return ResponseEntity.noContent().build();
    }
}
