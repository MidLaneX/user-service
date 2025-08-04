package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByOwnerId(Long ownerId);
    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
