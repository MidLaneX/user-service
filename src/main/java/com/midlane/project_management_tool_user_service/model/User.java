package com.midlane.project_management_tool_user_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;

    // Professional information
    private String jobTitle;
    private String department;

    // Team relationships
    private List<String> teamIds;
    private String managedTeamId; // For team leads

    // Status and preferences
    private UserStatus status;
    private boolean isActive;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    // Enums for better type safety
    public enum UserStatus {
        ACTIVE, INACTIVE, PENDING, SUSPENDED
    }

    // Computed property for display name
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return (firstName != null ? firstName : "") +
               (firstName != null && lastName != null ? " " : "") +
               (lastName != null ? lastName : "");
    }

    // Helper methods for team management
    public boolean isTeamLead() {
        return managedTeamId != null && !managedTeamId.isEmpty();
    }

    public boolean isMemberOfTeam(String teamId) {
        return teamIds != null && teamIds.contains(teamId);
    }
}
