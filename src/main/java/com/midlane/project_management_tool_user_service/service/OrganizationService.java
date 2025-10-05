package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.OrganizationResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationMemberResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationMemberBriefResponse;
import com.midlane.project_management_tool_user_service.dto.OrganizationTeamResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + ownerId));
        
        Organization organization = Organization.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .size(request.getSize())
                .location(request.getLocation())
                .owner(owner)
                .status(Organization.OrganizationStatus.ACTIVE)
                .build();

        Organization savedOrg = organizationRepository.save(organization);
        
        // Use the business method to properly add owner as member
        owner.createOrganization(savedOrg);
        userRepository.save(owner);

        log.info("Organization created: id={}, name={}, owner={}", 
                savedOrg.getId(), savedOrg.getName(), owner.getEmail());
        
        return mapToResponse(savedOrg);
    }

    @Transactional
    public OrganizationResponse updateOrganization(Long organizationId, CreateOrganizationRequest request, Long requesterId) {
        Organization existingOrg = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        
        // Check if requester is owner
        if (!existingOrg.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can update organization details");
        }
        
        // Update fields
        existingOrg.setName(request.getName());
        existingOrg.setDescription(request.getDescription());
        existingOrg.setWebsite(request.getWebsite());
        existingOrg.setIndustry(request.getIndustry());
        existingOrg.setSize(request.getSize());
        existingOrg.setLocation(request.getLocation());

        Organization savedOrg = organizationRepository.save(existingOrg);
        return mapToResponse(savedOrg);
    }

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        return mapToResponse(organization);
    }

    @Transactional
    public void addMember(Long organizationId, Long requesterId, String userEmail) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        
        // Check if requester is owner or admin
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can add members");
        }
        
        // Check if user is already a member
        if (organization.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member of this organization");
        }
        
        organization.addMember(user);
        organizationRepository.save(organization);
        
        log.info("User added to organization: userId={}, orgId={}, orgName={}", 
                userEmail, organizationId, organization.getName());
    }

    @Transactional
    public void removeMember(Long organizationId, Long userId, Long requesterId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check if requester is owner
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can remove members");
        }
        
        // Cannot remove owner
        if (organization.getOwner().equals(user)) {
            throw new RuntimeException("Cannot remove organization owner");
        }
        
        organization.removeMember(user);
        organizationRepository.save(organization);
        
        log.info("User removed from organization: userId={}, orgId={}, orgName={}", 
                userId, organizationId, organization.getName());
    }

    public List<Organization> getOrganizationsByOwner(Long ownerId) {
        return organizationRepository.findByOwnerId(ownerId);
    }

    public List<Organization> getOrganizationsByMember(Long userId) {
        return organizationRepository.findByMemberId(userId);
    }

    public List<Organization> searchOrganizations(String searchTerm) {
        return organizationRepository.findByNameContainingIgnoreCase(searchTerm);
    }

//    public Organization getOrganizationById(Long organizationId) {
//        return organizationRepository.findById(organizationId)
//                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
//    }

    public boolean isUserMember(Long organizationId, Long userId) {
        return organizationRepository.isUserMemberOfOrganization(organizationId, userId);
    }

    public boolean isUserOwner(Long organizationId, Long userId) {
        return organizationRepository.isUserOwnerOfOrganization(organizationId, userId);
    }

    @Transactional
    public void deleteOrganization(Long organizationId, Long requesterId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));
        
        // Check if requester is owner
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can delete organization");
        }
        
        organizationRepository.delete(organization);
        
        log.info("Organization deleted: id={}, name={}", organizationId, organization.getName());
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

    public List<OrganizationMemberResponse> getOrganizationMembers(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        return organization.getMembers().stream()
                .map(user -> OrganizationMemberResponse.builder()
                        .userId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .jobTitle(user.getJobTitle())
                        .department(user.getDepartment())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<OrganizationTeamResponse> getOrganizationTeams(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        return organization.getTeams().stream()
                .map(team -> OrganizationTeamResponse.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .teamType(team.getTeamType().toString())
                        .description(team.getDescription())
                        .memberCount(team.getMembers().size())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // NEW: Method for project service to get teams for dropdown
    public List<OrganizationTeamResponse> getTeamsByOrganization(Long organizationId) {
        return getOrganizationTeams(organizationId);
    }

    // NEW: Clear methods for frontend - Get organizations owned by user
    public List<OrganizationResponse> getOwnedOrganizations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return user.getOwnedOrganizations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // NEW: Clear methods for frontend - Get organizations where user is member (but not owner)
    public List<OrganizationResponse> getMemberOrganizations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return user.getOrganizations().stream()
                .filter(org -> !org.getOwner().getId().equals(userId)) // Exclude owned organizations
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // NEW: Get all organizations for a user (owned + member)
    public List<OrganizationResponse> getAllUserOrganizations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return user.getOrganizations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // IMPROVED: Better member management with proper relationship handling
    @Transactional
    public void addMemberById(Long organizationId, Long requesterId, Long userIdToAdd) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        User userToAdd = userRepository.findById(userIdToAdd)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userIdToAdd));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));

        // Check if requester is owner
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can add members");
        }

        // Check if user is already a member
        if (organization.getMembers().contains(userToAdd)) {
            throw new RuntimeException("User is already a member of this organization");
        }

        // Use business method to properly handle relationships
        userToAdd.joinOrganization(organization);
        userRepository.save(userToAdd);

        log.info("User added to organization: userId={}, orgId={}, orgName={}",
                userIdToAdd, organizationId, organization.getName());
    }

    // IMPROVED: Better member removal with proper relationship handling
    @Transactional
    public void removeMemberImproved(Long organizationId, Long userId, Long requesterId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if requester is owner
        if (!organization.getOwner().getId().equals(requesterId)) {
            throw new RuntimeException("Only organization owner can remove members");
        }

        // Cannot remove owner
        if (organization.getOwner().equals(user)) {
            throw new RuntimeException("Cannot remove organization owner");
        }

        // Use business method to properly handle relationships including teams
        user.leaveOrganization(organization);
        userRepository.save(user);

        log.info("User removed from organization: userId={}, orgId={}, orgName={}",
                userId, organizationId, organization.getName());
    }

    public List<OrganizationMemberBriefResponse> getOrganizationMembersBrief(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + organizationId));

        return organization.getMembers().stream()
                .map(user -> OrganizationMemberBriefResponse.builder()
                        .userId(user.getUserId())
                        .name(user.getFullName()) // Use the business method for better name handling
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
