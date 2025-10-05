package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.OrganizationResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationMemberResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationMemberBriefResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationTeamResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @RequestParam Long ownerId) {
        OrganizationResponse response = organizationService.createOrganization(request, ownerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrganizationRequest request,
            @RequestParam Long requesterId) {
        OrganizationResponse response = organizationService.updateOrganization(id, request, requesterId);
        return ResponseEntity.ok(response);
    }

    // NEW: Get teams for organization (for project service dropdown)
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<OrganizationTeamResponse>> getTeamsByOrganization(@PathVariable Long id) {
        List<OrganizationTeamResponse> teams = organizationService.getTeamsByOrganization(id);
        return ResponseEntity.ok(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
        organizationService.deleteOrganization(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/add")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @RequestParam Long requesterId,
            @RequestParam String userEmail) {
        organizationService.addMember(id, requesterId, userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {
        organizationService.removeMemberImproved(id, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // DEPRECATED - Use /users/{userId}/organizations/owned instead
    @GetMapping("/owner/{ownerId}")
    @Deprecated
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsByOwner(@PathVariable Long ownerId) {
        List<OrganizationResponse> organizations = organizationService.getOwnedOrganizations(ownerId);
        return ResponseEntity.ok(organizations);
    }

    // NEW: Clear API - Get organizations owned by user
    @GetMapping("/users/{userId}/owned")
    public ResponseEntity<List<OrganizationResponse>> getOwnedOrganizations(@PathVariable Long userId) {
        List<OrganizationResponse> organizations = organizationService.getOwnedOrganizations(userId);
        return ResponseEntity.ok(organizations);
    }

    // NEW: Clear API - Get organizations where user is member (but not owner)
    @GetMapping("/users/{userId}/member")
    public ResponseEntity<List<OrganizationResponse>> getMemberOrganizations(@PathVariable Long userId) {
        List<OrganizationResponse> organizations = organizationService.getMemberOrganizations(userId);
        return ResponseEntity.ok(organizations);
    }

    // NEW: Clear API - Get all organizations for user (owned + member)
    @GetMapping("/users/{userId}/all")
    public ResponseEntity<List<OrganizationResponse>> getAllUserOrganizations(@PathVariable Long userId) {
        List<OrganizationResponse> organizations = organizationService.getAllUserOrganizations(userId);
        return ResponseEntity.ok(organizations);
    }

    // NEW: Add member by user ID instead of email
    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> addMemberById(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {
        organizationService.addMemberById(id, requesterId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{organizationId}/members")
    public ResponseEntity<List<OrganizationMemberResponse>> getOrganizationMembers(@PathVariable Long organizationId) {
        List<OrganizationMemberResponse> members = organizationService.getOrganizationMembers(organizationId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}/teams-detailed")
    public ResponseEntity<List<OrganizationTeamResponse>> getOrganizationTeams(@PathVariable Long id) {
        List<OrganizationTeamResponse> teams = organizationService.getOrganizationTeams(id);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{organizationId}/members/brief")
    public ResponseEntity<List<OrganizationMemberBriefResponse>> getOrganizationMembersBrief(@PathVariable Long organizationId) {
        List<OrganizationMemberBriefResponse> membersBrief = organizationService.getOrganizationMembersBrief(organizationId);
        return ResponseEntity.ok(membersBrief);
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .website(organization.getWebsite())
                .industry(organization.getIndustry())
                .size(organization.getSize())
                .location(organization.getLocation())
                .ownerId(organization.getOwner().getId())
                .ownerEmail(organization.getOwner().getEmail())
                .status(organization.getStatus().toString())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .memberCount(organization.getMembers().size())
                .teamCount(organization.getTeams().size())
                .build();
    }
}
