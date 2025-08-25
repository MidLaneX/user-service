package com.midlane.project_management_tool_user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"teams", "members"})
@ToString(exclude = {"teams", "members"})
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "website")
    private String website;

    @Column(name = "industry")
    private String industry;

    @Column(name = "size")
    private String size;

    @Column(name = "location")
    private String location;

    // Owner relationship - Many organizations can have the same owner user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // One-to-many relationship with teams
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Team> teams = new HashSet<>();

    // Many-to-many relationship with users (organization members)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "organization_members",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"})
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    // Business methods
    public void addTeam(Team team) {
        teams.add(team);
        team.setOrganization(this);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
        team.setOrganization(null);
    }

    public void addMember(User user) {
        members.add(user);
        user.getOrganizations().add(this);
    }

    public void removeMember(User user) {
        members.remove(user);
        user.getOrganizations().remove(this);
    }

    public boolean isOwner(User user) {
        return owner != null && owner.equals(user);
    }

    public enum OrganizationStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }
}
