package com.midlane.project_management_tool_user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TeamProjectId.class)
public class TeamProject {

    @Id
    @Column(name = "team_id")
    private Long teamId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_project_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_team_project_project"))
    private Project project;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}
