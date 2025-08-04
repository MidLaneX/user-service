package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email);

    boolean existsByUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username);
}