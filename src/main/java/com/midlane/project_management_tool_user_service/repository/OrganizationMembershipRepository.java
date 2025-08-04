package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.OrganizationMembership;
import com.midlane.project_management_tool_user_service.model.OrganizationMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, OrganizationMembershipId> {

    List<OrganizationMembership> findByOrganizationId(Long organizationId);
    List<OrganizationMembership> findByUserId(Long userId);
    boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);
}
