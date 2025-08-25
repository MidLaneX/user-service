package com.midlane.project_management_tool_user_service.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"ownedOrganizations", "organizations", "teams", "ledTeams"})
@ToString(exclude = {"ownedOrganizations", "organizations", "teams", "ledTeams"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false, nullable = false)
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash; // Made nullable for social login users

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Now references Role entity

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE; // Default to ACTIVE

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false; // Default to false

    @Column(name = "password_last_changed")
    private LocalDateTime passwordLastChanged;

    @Column(name = "email_last_changed")
    private LocalDateTime emailLastChanged;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Social login fields
    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL; // Default to local

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // Organizations owned by this user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Organization> ownedOrganizations = new HashSet<>();

    // Organizations this user is a member of
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Organization> organizations = new HashSet<>();

    // Teams this user is a member of
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Team> teams = new HashSet<>();

    // Teams this user leads
    @OneToMany(mappedBy = "teamLead", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Team> ledTeams = new HashSet<>();

    // Convenience method for compatibility
    public Long getId() {
        return this.userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    // Business methods for organization management
    public void createOrganization(Organization organization) {
        ownedOrganizations.add(organization);
        organization.setOwner(this);
        // Auto-add owner as member
        organizations.add(organization);
        organization.getMembers().add(this);
    }

    public void joinOrganization(Organization organization) {
        organizations.add(organization);
        organization.getMembers().add(this);
    }

    public void leaveOrganization(Organization organization) {
        organizations.remove(organization);
        organization.getMembers().remove(this);

        // Also leave all teams in that organization
        teams.removeIf(team -> team.getOrganization().equals(organization));
    }

    public void joinTeam(Team team) {
        teams.add(team);
        team.getMembers().add(this);
    }

    public void leaveTeam(Team team) {
        teams.remove(team);
        team.getMembers().remove(this);

        // If user was leading the team, remove leadership
        if (team.getTeamLead() != null && team.getTeamLead().equals(this)) {
            team.setTeamLead(null);
            ledTeams.remove(team);
        }
    }

    public void becomeTeamLead(Team team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException("User must be a team member before becoming team lead");
        }
        ledTeams.add(team);
        team.setTeamLead(this);
    }

    public boolean isOwnerOf(Organization organization) {
        return ownedOrganizations.contains(organization);
    }

    public boolean isMemberOf(Organization organization) {
        return organizations.contains(organization);
    }

    public boolean isMemberOfTeam(Team team) {
        return teams.contains(team);
    }

    public boolean isLeaderOfTeam(Team team) {
        return ledTeams.contains(team);
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return email;
        }
    }

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        PENDING_VERIFICATION,
        PENDING
    }
}
