package com.midlane.project_management_tool_user_service.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"members"})
@ToString(exclude = {"members"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_type")
    @Builder.Default
    private TeamType teamType = TeamType.DEVELOPMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 10;

    // Belongs to an organization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Team lead/manager (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_lead_id")
    private User teamLead;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-many relationship with users (team members)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"})
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    // Business methods
    public void addMember(User user) {
        if (hasAvailableSlots()) {
            members.add(user);
            user.getTeams().add(this);
        } else {
            throw new IllegalStateException("Team has reached maximum capacity");
        }
    }

    public void removeMember(User user) {
        members.remove(user);
        user.getTeams().remove(this);

        // If removed user was team lead, clear the lead
        if (teamLead != null && teamLead.equals(user)) {
            teamLead = null;
        }
    }

    public void setTeamLead(User user) {
        if (!members.contains(user)) {
            throw new IllegalArgumentException("Team lead must be a member of the team");
        }
        this.teamLead = user;
    }

    public boolean hasAvailableSlots() {
        return members.size() < maxMembers;
    }

    public int getCurrentMemberCount() {
        return members.size();
    }

    public boolean isTeamLead(User user) {
        return teamLead != null && teamLead.equals(user);
    }

    public boolean isMember(User user) {
        return members.contains(user);
    }

    public enum TeamType {
        DEVELOPMENT, DESIGN, MARKETING, SALES, SUPPORT, MANAGEMENT, QA, DEVOPS, RESEARCH
    }

    public enum TeamStatus {
        ACTIVE, INACTIVE, DISBANDED, PENDING
    }
}
