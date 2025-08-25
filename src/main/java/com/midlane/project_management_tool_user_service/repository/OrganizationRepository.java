package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.Organization;
import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    // Find organizations by owner
    List<Organization> findByOwner(User owner);

    // Find organizations by owner ID
    List<Organization> findByOwnerId(Long ownerId);

    // Find organizations by name (case-insensitive)
    List<Organization> findByNameContainingIgnoreCase(String name);

    // Find organizations by status
    List<Organization> findByStatus(Organization.OrganizationStatus status);

    // Find organizations where user is a member
    @Query("SELECT o FROM Organization o JOIN o.members m WHERE m.id = :userId")
    List<Organization> findByMemberId(@Param("userId") Long userId);

    // Check if user is member of organization
    @Query("SELECT COUNT(o) > 0 FROM Organization o JOIN o.members m WHERE o.id = :orgId AND m.id = :userId")
    boolean isUserMemberOfOrganization(@Param("orgId") Long orgId, @Param("userId") Long userId);

    // Check if user is owner of organization
    @Query("SELECT COUNT(o) > 0 FROM Organization o WHERE o.id = :orgId AND o.owner.id = :userId")
    boolean isUserOwnerOfOrganization(@Param("orgId") Long orgId, @Param("userId") Long userId);

    // Find organizations by industry
    List<Organization> findByIndustryContainingIgnoreCase(String industry);

    // Count organizations by owner
    long countByOwner(User owner);
}
