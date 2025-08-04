package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.AddTeamMemberRequest;
import com.midlane.project_management_tool_user_service.dto.UserResponse;
import com.midlane.project_management_tool_user_service.service.TeamMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMembershipController {

    private final TeamMembershipService teamMembershipService;

    @PostMapping
    public ResponseEntity<Void> addTeamMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request) {
        teamMembershipService.addTeamMember(teamId, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getTeamMembers(@PathVariable Long teamId) {
        List<UserResponse> members = teamMembershipService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        teamMembershipService.removeTeamMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }
}
