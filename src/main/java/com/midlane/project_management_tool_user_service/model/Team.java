package com.midlane.project_management_tool_user_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;
    private String departmentId;
    private String teamLeadId;

    // Team configuration
    private TeamType type;
    private TeamStatus status;
    private int maxMembers;

    // Member relationships
    private List<String> memberIds;
    private List<String> projectIds;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String createdBy;

    // Enums for better type safety
    public enum TeamType {
        DEVELOPMENT, DESIGN, MARKETING, SALES, SUPPORT, MANAGEMENT, QA, DEVOPS
    }

    public enum TeamStatus {
        ACTIVE, INACTIVE, DISBANDED, PENDING
    }

    // Computed properties
    public int getCurrentMemberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }

    public boolean hasAvailableSlots() {
        return getCurrentMemberCount() < maxMembers;
    }
}
