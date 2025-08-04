package com.midlane.project_management_tool_user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TeamMembershipId.class)
public class TeamMembership {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_membership_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_membership_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_membership_role"))
    private Role role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
