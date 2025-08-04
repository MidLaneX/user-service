package com.midlane.project_management_tool_user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "team_lead_id")
    private String teamLeadId;

    @Column(name = "managed_team_id")
    private String managedTeamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TeamType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamStatus status;

    @Column(name = "max_members")
    private int maxMembers;

    @ElementCollection
    @CollectionTable(name = "team_member_ids", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "member_id")
    private List<String> memberIds;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_organization"))
    private Organization organization;

    // Business logic methods that TeamServiceImpl expects
    public boolean hasAvailableSlots() {
        return memberIds != null ? memberIds.size() < maxMembers : maxMembers > 0;
    }

    public int getCurrentMemberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }

    // Enums for better type safety
    public enum TeamType {
        DEVELOPMENT, DESIGN, MARKETING, SALES, SUPPORT, MANAGEMENT, QA, DEVOPS
    }

    public enum TeamStatus {
        ACTIVE, INACTIVE, DISBANDED, PENDING
    }
}
