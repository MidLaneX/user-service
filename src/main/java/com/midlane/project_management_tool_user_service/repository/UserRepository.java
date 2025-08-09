package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);

    boolean existsByAuthServiceUserId(Long authServiceUserId);

    User findByEmail(String email);

    User findByAuthServiceUserId(Long authServiceUserId);
}