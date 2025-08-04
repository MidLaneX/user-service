package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.CreateOrganizationRequest;
import com.midlane.project_management_tool_user_service.dto.OrganizationResponse;
import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.repository.OrganizationRepository;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationResponse createOrganization(CreateOrganizationRequest request, Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new RuntimeException("Owner user not found");
        }

        if (organizationRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new RuntimeException("Organization with this name already exists for this owner");
        }

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setOwnerId(ownerId);

        Organization savedOrganization = organizationRepository.save(organization);
        return mapToOrganizationResponse(savedOrganization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToOrganizationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return mapToOrganizationResponse(organization);
    }

    public OrganizationResponse updateOrganization(Long id, CreateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check if name already exists for this owner (excluding current organization)
        if (!organization.getName().equals(request.getName()) &&
            organizationRepository.existsByNameAndOwnerId(request.getName(), organization.getOwnerId())) {
            throw new RuntimeException("Organization with this name already exists for this owner");
        }

        organization.setName(request.getName());
        organization.setDescription(request.getDescription());

        Organization savedOrganization = organizationRepository.save(organization);
        return mapToOrganizationResponse(savedOrganization);
    }

    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found");
        }
        organizationRepository.deleteById(id);
    }

    private OrganizationResponse mapToOrganizationResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setDescription(organization.getDescription());
        response.setOwnerId(organization.getOwnerId());
        response.setCreatedAt(organization.getCreatedAt());
        return response;
    }
}
