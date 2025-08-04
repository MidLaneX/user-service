package com.midlane.project_management_tool_user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrganizationMembershipId.class)
public class OrganizationMembership {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_org_membership_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_org_membership_organization"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_org_membership_role"))
    private Role role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
